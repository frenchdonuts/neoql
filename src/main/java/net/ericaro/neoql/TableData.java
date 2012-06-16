package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;



/**
 * basic table, essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class TableData<T> implements Table<T> {

	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	private TableListenerSupport<T> internals = new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)
	private ColumnDef<T, ?>[] columns;
	private List<T> rows = new ArrayList<T>();
	private Class<T> type;
	//private ClassTableDef<T> table;
	private Database owner;
	private TableListener[] internalColumnListeners;
	private TableListener[] columnListeners;

	

	TableData(Database owner, ClassTableDef<T> table) {
		this.owner = owner;
		this.type = table.getTable();
		
		// copy columns
		ColumnDef[] cols = table.columns;
		this.columns = new ColumnDef[cols.length];
		System.arraycopy(cols, 0, columns, 0, cols.length);
		
		this.internalColumnListeners = new TableListener[this.columns.length];
		this.columnListeners = new TableListener[this.columns.length];
		

	}

	
	
	@Override
	public void drop() {
		uninstall() ;
		this.rows.clear();
		internals.fireDrop(this);
		events.fireDrop(this);
	}


	void install() {
		int i = 0;
		for (Column<T, ?> col : columns)
			installColumn(i++, col);
	}

	void uninstall() {
		int i = 0;
		for (Column<T, ?> col : columns)
			unInstallColumn(i++, col);
		if (internals.getListenerCount() > 0)
			throw new NeoQLException("Cannot drop table " + type
					+ ". Constraint violation(s)" + internals);
		
	}

	private <V> void installColumn(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			internalColumnListeners[i] = new ForeignKeyColumnListener<V>(col);
			owner.addInternalTableListener(col.getForeignTable(), internalColumnListeners[i]);
			
			columnListeners[i] = new AbstractTableListener<V>() {
				public void updated(V oldRow, V newRow) {
					fireUpdate();
				}
			};
			owner.addTableListener(col.getForeignTable(), columnListeners[i]);
			
		}
	}

	private <V> void unInstallColumn(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			owner.removeInternalTableListener(col.getForeignTable(),
					internalColumnListeners[i]);
			owner.removeTableListener(col.getForeignTable(),
					columnListeners[i]);
			
		}
	}

	public void addTableListener(TableListener<T> listener) {
		events.addTableListener(listener);
	}

	@Override
	public void removeTableListener(TableListener<T> listener) {
		events.removeTableListener(listener);
	}

	void addInternalTableListener(TableListener<T> listener) {
		internals.addTableListener(listener);
	}

	void removeInternalTableListener(TableListener<T> listener) {
		internals.removeTableListener(listener);
	}

	class ForeignKeyColumnListener<V> extends AbstractTableListener<V> {

		private Column<T, V> col;

		ForeignKeyColumnListener(Column<T, V> col) {
			super();
			this.col = col;
		}

		@Override
		public void updated(final V oldValue, final V newValue) {
			if (oldValue == newValue)
				return;
			
			Predicate<T> p = NeoQL.is(col, oldValue) ;
			ColumnValue<T, ?>[] setters = new ColumnValue[] {
					new ColumnValue<T, V>(col, newValue)
			};
			for(T row: rows)
				if(p.eval(row))
					update(row, setters);
		}

		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = NeoQL.is(col, oldValue);
			for (T t : owner.select(TableData.this) )
				if (inUse.eval(t))
					throw new NeoQLException("Foreign Key violation" + col);
		}
		@Override
		public void dropped(Table<V> table) {
			drop();
		}
		@Override
		public String toString() {
			return "Foreign Key:" + type +  " → "
					+ col.getForeignTable() + ".";
		}
	}
	
	
	Database getOwner() {
		return owner;
	}
	
	public Class<T> getType() {
		return type;
	}

	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}

	T append(T row) {
		rows.add(row);// todo do some check here
		return row;
	}

	Map<T, T> updatedRows = new HashMap<T, T>();

	T update(T row, ColumnValue<T, ?>[] setters) {
			T clone;
			if (updatedRows.containsKey(row))
				clone = row;
			else {
				clone = clone(row);
				updatedRows.put(clone, row);
			}
			
			for (ColumnValue s : setters)
				s.set(clone);

			int i = rows.indexOf(row);
			rows.set(i, clone);
			internals.fireUpdated(row, clone);
			return clone;
	}
	
	void update(T row, T clone) {
		assert !updatedRows.containsKey(row) : "update with an instance should only be called first";
		updatedRows.put(clone, row);
		int i = rows.indexOf(row);
		rows.set(i, clone);
		internals.fireUpdated(row, clone); 
	}
	
	void fireUpdate() {
		HashSet<Entry<T, T>> temp = new HashSet<Entry<T, T>>(updatedRows.entrySet() );// copy entries
		updatedRows.clear(); // clear before causing any reintrant calls
		for (Entry<T, T> e : temp )
			events.fireUpdated(e.getValue(), e.getKey());
	}
	

	private boolean contains(T value) {
		return rows.contains(value);
	}

	
	void delete(Predicate<? super T> where) {
		for (ListIterator<T> i = rows.listIterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row)) {
				i.remove();
				internals.fireDeleted(row); // eventually I'll had to use the same protocol as for the update operation. But for now, there is no need, internals only fire exeception
				events.fireDeleted(row);
			}
		}
	}
	
	

	T insert(T row) {
		rows.add(row);
		internals.fireInserted(row);
		events.fireInserted(row);
		return row;
	}

	
	/** creates an instance suitable for this table data, with the given columns set
	 * 
	 * @param data
	 * @param values
	 * @return
	 */
	T newInstance(ColumnValue<T,?>... values) {
		
		try {
			T row  = type.newInstance();
			assert allSameType(values): "cannot use columns values from another type";
			for (ColumnValue s : values)
				s.set(row);
			// TODO handle default values (like ids etc, so I need to 'update' the object a little bit and return it

			return row;
		} catch (Exception e) {
			throw new NeoQLException("Exception while instanciating row for table " + type, e);
		}
	}

	public T clone(T row) {
		T clone = newInstance();
		for (ColumnDef<T, ?> c : columns )
			c.copy(row, clone);
		return clone;
	}	

	// helper method to asser that all columnvalue have the same class definition
	private boolean allSameType(ColumnValue<T, ?>... values) {
		if (values.length == 0)
			return true;
		for (ColumnValue cv : values)
			if (! type.equals(cv.column.getTable().getTable()))
				return false;
		return true;
	}
		
		
}
