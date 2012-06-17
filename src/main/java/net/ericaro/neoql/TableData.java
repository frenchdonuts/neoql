package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.EventListenerList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;




/**
 * basic table, essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class TableData<T> implements Table<T> {

	// public events
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	// internal ones, means between tables through foreign keys only
	private TableListenerSupport<T> internals = new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)
	
	private ColumnDef<T, ?>[] columns;
	private Set<T> rows = new HashSet<T>();
	private Class<T> type;
	private Database owner;
	
	private TableListener[] internalColumnListeners;

	// transactions
	MyDeleteChange	deleteOperation = null; //
	MyInsertChange	insertOperation = null;
	MyUpdateChange	updateOperation = null;

	

	TableData(Database owner, ClassTableDef<T> table) {
		this.owner = owner;
		this.type = table.getTable();
		
		// copy columns
		ColumnDef[] cols = table.columns;
		this.columns = new ColumnDef[cols.length];
		System.arraycopy(cols, 0, columns, 0, cols.length);
		
		this.internalColumnListeners = new TableListener[this.columns.length];
		

	}

	
	@Override
	public void drop() {
		uninstall() ;
		this.rows.clear();
		fireDrop(this);
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
			TableData<V> ftable = owner.get(col.getForeignTable().table);
			internalColumnListeners[i] = new ForeignKeyColumnListener<V>(col);
			owner.addInternalTableListener(ftable, internalColumnListeners[i]);
		}
	}

	private <V> void unInstallColumn(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			TableData<V> ftable = owner.get(col.getForeignTable().table);
			owner.removeInternalTableListener(ftable,
					internalColumnListeners[i]);
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
			// the foreign key has been updated
			if (oldValue == newValue) return; // it's a false change, take a shortcut ( when is that happenning, infact ?)
			
			// now we are going to update every row that points to the oldValue
			Predicate<T> p = NeoQL.is(col, oldValue) ;
			ColumnValue<T, ?>[] setters = new ColumnValue[] {
					new ColumnValue<T, V>(col, newValue)
			};
			
			if (updateOperation == null )
				updateOperation = new MyUpdateChange();
			updateOperation.update(p, setters);
		}

		
		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = NeoQL.is(col, oldValue);
			for (T t : NeoQL.select(TableData.this) )
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
	

	// ##########################################################################
	// GETTER BEGIN
	// ##########################################################################
	Database getOwner() {
		return owner;
	}
	
	public Class<T> getType() {
		return type;
	}
	// ##########################################################################
	// GETTER END
	// ##########################################################################

	
	
	
	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}
	
	
	
	// ##########################################################################
	// UPDATE END
	// ##########################################################################
	
	
	T update(T oldValue, ColumnValue<T, ?>[] setters) {
		if (updateOperation == null )	
			updateOperation = new MyUpdateChange();
		return updateOperation.update(oldValue, setters);
	}
	void update(Predicate<T> p, ColumnValue<T, ?>[] setters) {
		if (updateOperation == null )	
			updateOperation = new MyUpdateChange();
		updateOperation.update(p, setters);
	}
	
	// ##########################################################################
	// UPDATE END
	// ##########################################################################
	
	
	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################
	
	/** delete operation, fill the delete Operation accordingly
	 * 
	 * @param where
	 */
	void delete(Predicate<? super T> where) {
		if (deleteOperation == null )
			deleteOperation = new MyDeleteChange();
		
		for (Iterator<T> i = rows.iterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row))
				deleteOperation.delete(row);
		}
	}
	
	// ##########################################################################
	// DELETE END
	// ##########################################################################
	
	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################

	T insert(T row) {
		if (insertOperation == null)
			insertOperation = new MyInsertChange();
		insertOperation.insert(row);
		return row;
	}

	// ##########################################################################
	// INSERT END
	// ##########################################################################
	
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
		

	
	// ##########################################################################
	// OPERATIONS BEGIN
	// ##########################################################################

	class MyUpdateChange extends UpdateChange<T>{
		transient Map<T,T> updatedRows = new HashMap<T,T>(); 
		
		public void update(Predicate<T> p, ColumnValue<T, ?>[] setters) {
			Set<T> updated = new HashSet<T>();// we first compute the rows involved
			for(T row: rows) // not rows but the virtual new rows
				if(p.eval(row) && ! updatedRows.containsKey(row))  // match, and it has not changed
					updated.add(row);
			for(T row: updatedRows.values()) // also need to update the 'new' ones
				if(p.eval(row) )  // match, and it has not changed
					updated.add(row);
				
			// now that we have the set of rows involved in the update
			for (T row : updated)
				update(row, setters);
		}
		
		public T update(T oldValue, ColumnValue<T, ?>[] setters) {

			T newValue ; 
			// fix point algorithm, an updated row can trigger internal events that cause it self to change.
			// so we keep the map of old ->new so that the new one is created once, and edited subsequently
			if (updatedRows.containsValue(oldValue)) // the the oldvalue is a new value, this means that the value as already been updated
				newValue = oldValue;
			else {
				newValue = TableData.this.clone(oldValue);
				updatedRows.put(oldValue, newValue);
			}
			
			for (ColumnValue s : setters)
				s.set(newValue);
			
			update(oldValue, newValue);
			// fire internal events so that other rows might want to keep in touch
			internals.fireUpdated(oldValue, newValue);
			return newValue;
		}
		
		@Override
		public void revert() {
			for (Pair<T,T> r :updated) {
				rows.remove(r.getRight()); // remove the new
				rows.add(r.getLeft()); // add the old
				events.fireUpdated(r.getRight(),r.getLeft());
			}
		}
		
		@Override
		public void commit() {
			for (Pair<T,T> r :updated) {
				rows.remove(r.getLeft()); // remove the new
				rows.add(r.getRight()); // add the old
				events.fireUpdated(r.getLeft(),r.getRight());
			}
		}
	}
	
	
	class MyInsertChange extends InsertChange<T>{
			
			@Override
			public void revert() {
				for (T r :inserted) 
					if (rows.remove(r))// actually remove the items
						fireDeleted(r);
			}
			@Override
			public void commit() {
				for (T r :inserted) 
					if (rows.add(r))// actually add the item back
						fireInserted(r);
			}
			
		}
		
	class MyDeleteChange extends DeleteChange<T>{
			public void revert() {
				for (T r :deleted) 
					if (rows.add(r))// actually remove the items
						fireInserted(r);
			}
			// register deletion
			
			public void commit() {
				for (T r :deleted) 
					if (rows.remove(r))// actually add the item back
						fireDeleted(r);
			}
		}
	    
	    public void fireDeleted(T row) {
	    	internals.fireDeleted(row);
			events.fireDeleted(row);
		}



		public void fireInserted(T row) {
			internals.fireInserted(row);
			events.fireInserted(row);
		}

		public void fireDrop(Table<T> table) {
			internals.fireDrop(table);
			events.fireDrop(table);
		}
	    
		// ##########################################################################
		// UNDO END
		// ##########################################################################

	
}
