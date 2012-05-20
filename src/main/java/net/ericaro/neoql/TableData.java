package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * basic table, essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class TableData<T> implements Table<T> {

	TableListenerSupport<T> events = new TableListenerSupport<T>();
	TableListenerSupport<T> internals = new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)

	AbstractColumn<T, ?>[] columns;
	List<T> rows = new ArrayList<T>();
	//private Class<T> type;
	private ClassTableDef<T> table;
	private Database owner;
	private TableListener[] internalColumnListeners;
	private TableListener[] columnListeners;

	

	TableData(Database owner, ClassTableDef<T> table) {
		this.owner = owner;
		this.table = table;
		Column[] cols = table.getColumns();
		
		this.columns = new AbstractColumn[cols.length];
		System.arraycopy(cols, 0, columns, 0, cols.length);
		
		this.internalColumnListeners = new TableListener[this.columns.length];
		this.columnListeners = new TableListener[this.columns.length];
		

	}

	
	
	@Override
	public void drop(Database from) {
		from.drop(table);
	}


	void install() {
		int i = 0;
		for (AbstractColumn<T, ?> col : columns)
			installColumn(i++, col);
	}

	void uninstall() {
		int i = 0;
		for (AbstractColumn<T, ?> col : columns)
			unInstallColumn(i++, col);
		if (internals.getListenerCount() > 0)
			throw new NeoQLException("Cannot drop table " + table
					+ ". Constraint violation(s)" + internals);

	}

	private <V> void installColumn(int i, AbstractColumn<T, V> col) {
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

	private <V> void unInstallColumn(int i, AbstractColumn<T, V> col) {
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

		private AbstractColumn<T, V> col;

		ForeignKeyColumnListener(AbstractColumn<T, V> col) {
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
			for (T t : owner.select(table) )
				if (inUse.eval(t))
					throw new NeoQLException("Foreign Key violation" + col);
		}

		@Override
		public String toString() {
			return "Foreign Key:" + table +  " → "
					+ col.getForeignTable() + ".";
		}
	}

	Database getOwner() {
		return owner;
	}

	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}

	T append(T row) {
		rows.add(row);// todo do some check here
		return row;
	}

	T clone(T row) {
			T clone = table.newInstance();
			for (AbstractColumn<T, ?> c : columns)
				c.copy(row, clone);
			return clone;
	}

	Map<T, T> updatedRows = new HashMap<T, T>();

	void update(T row, ColumnValue<T, ?>[] setters) {
			T clone;
			if (updatedRows.containsKey(row))
				clone = row;
			else {
				clone = clone(row);
				updatedRows.put(clone, row);
			}

			for (ColumnValue s : setters) {
				s.set(clone);
			}

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
				events.fireInserted(row);
			}
		}
	}
	
	

	T insert(T row) {
		rows.add(row);
		internals.fireInserted(row);
		return row;
	}

}
