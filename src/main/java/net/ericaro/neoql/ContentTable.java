package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.ericaro.neoql.changeset.DeleteChange;
import net.ericaro.neoql.changeset.InsertChange;
import net.ericaro.neoql.changeset.UpdateChange;
import net.ericaro.neoql.eventsupport.AbstractTableListener;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;




/**
 * basic table, essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class ContentTable<T> implements Table<T> {

	// public events
	private TableListenerSupport<T> events = new TableListenerSupport<T>();
	// internal ones, means between tables through foreign keys only
	private TableListenerSupport<T> internals = new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)
	
	private MyColumn<T, ?>[] columns;
	private Set<T> rows = new HashSet<T>();
	private Class<T> type;
	private Database owner;
	
	private TableListener[] internalColumnListeners;

	// transactions
	MyDeleteChange	deleteOperation = null; 
	MyInsertChange	insertOperation = null;
	MyUpdateChange	updateOperation = null;

	

	ContentTable(Database owner, Class<T> table, Column[] cols) {
		this.owner = owner;
		this.type = table;
		
		// copy columns
		this.columns = new MyColumn[cols.length];
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
			connectForeignKey(i++, col);
	}

	void uninstall() {
		int i = 0;
		for (Column<T, ?> col : columns)
			disconnectForeignKey(i++, col);
		if (internals.getListenerCount() > 0)
			throw new NeoQLException("Cannot drop table " + type
					+ ". Constraint violation(s)" + internals);
		
	}

	private <V> void connectForeignKey(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			ContentTable<V> ftable = owner.get(col.getType());
			internalColumnListeners[i] = new ForeignKeyColumnListener<V>(col);
			owner.addInternalTableListener(ftable, internalColumnListeners[i]);
		}
	}

	private <V> void disconnectForeignKey(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			ContentTable<V> ftable = owner.get(col.getType());
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
			//(kind of execute update where vol is oldValue)
			Predicate<T> p = col.is(oldValue) ;
			ColumnSetter<T, ?>[] setters = new ColumnSetter[] {
					new ColumnSetter<T, V>(col, newValue)
			};
			getUpdateOperation().update(p, setters);
		}

		
		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse =col.is(oldValue);
			for (T t : NeoQL.select(ContentTable.this) )
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
					+ col.getType() + ".";
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
	
	
	T update(T oldValue, ColumnSetter<T, ?>[] setters) {
		return getUpdateOperation().update(oldValue, setters);
	}
	void update(Predicate<T> p, ColumnSetter<T, ?>[] setters) {
		getUpdateOperation().update(p, setters);
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
		for (Iterator<T> i = rows.iterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row)) {
				getDeleteOperation().delete(row);
				internals.fireDeleted(row);
			}
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
		internals.fireInserted(row);
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
	T newInstance(ColumnSetter<T,?>... values) {
		
		try {
			T row  = type.newInstance();
			assert allSameType(values): "cannot use columns values from another type";
			for (ColumnSetter s : values)
				s.set(row);
			// TODO handle default values (like ids etc, so I need to 'update' the object a little bit and return it

			return row;
		} catch (Exception e) {
			throw new NeoQLException("Exception while instanciating row for table " + type, e);
		}
	}

	public T clone(T row) {
		T clone = newInstance();
		for (MyColumn<T, ?> c : columns )
			c.copy(row, clone);
		return clone;
	}	

	// helper method to asser that all columnvalue have the same class definition
	private boolean allSameType(ColumnSetter<T, ?>... values) {
		if (values.length == 0)
			return true;
		for (ColumnSetter cv : values)
			if (! type.equals(cv.column.getTable()))
				return false;
		return true;
	}
		
	/** include transaction
	 * 
	 * @return
	 */
	Iterable<T> newRowsWhere(Predicate<T> p){
		
		Set<T> updated = new HashSet<T>();
		
		for(T row: rows) // not rows but the virtual new rows
			if(
				p.eval(row) // matches the predicate 
				&& ! getUpdateOperation().containsOld(row) // but it has not changed
				&& ! getDeleteOperation().contains(row)
				)
				updated.add(row); // old plain value
		// now handle updated
		for(T row: getUpdateOperation().newValues()) // also need to update the 'new' ones
			if(p.eval(row) )  // match, and it has not changed
				updated.add(row);
		// note that updated should not contains deleted (the operation should have been checked before
		
		// and finally inserted
		for (T row: getInsertOperation().values())
			if(p.eval(row) )  // the new ones matches
				updated.add(row);
		
		return Collections.unmodifiableCollection(updated);
	}
	
	// ##########################################################################
	// OPERATIONS BEGIN
	// ##########################################################################

	MyInsertChange getInsertOperation(){
		if (insertOperation == null )	
			insertOperation = new MyInsertChange();
		return insertOperation ;
	}
	
	MyDeleteChange getDeleteOperation(){
		if (deleteOperation == null )	
			deleteOperation = new MyDeleteChange();
		return deleteOperation ;
	}
	
	MyUpdateChange getUpdateOperation(){
		if (updateOperation == null )	
			updateOperation = new MyUpdateChange();
		return updateOperation ;
	}
	class MyUpdateChange extends UpdateChange<T>{
		
		public void update(Predicate<T> p, ColumnSetter<T, ?>[] setters) {
			// now that we have the set of rows involved in the update
			for (T row : newRowsWhere(p))
				update(row, setters);
		}
		
		public T update(T oldValue, ColumnSetter<T, ?>[] setters) {

			T newValue ; 
			
			// fix point algorithm, an updated row can trigger internal events that cause it self to change.
			// so we keep the map of old ->new so that the new one is created once, and edited subsequently
			
			//it won't scale up if we have an already huge transition.
			
			if (containsNew(oldValue)) // the the oldvalue is a new value, this means that the value as already been updated
				newValue = oldValue;
			else
				newValue = ContentTable.this.clone(oldValue);
			
			boolean changed = false;
			for (ColumnSetter s : setters)
				changed |= s.set(newValue);
			
			if (changed) {
				update(oldValue, newValue);
				// fire internal events so that other rows might want to keep in touch
				internals.fireUpdated(oldValue, newValue);
			}
			return newValue;
		}
		
		@Override
		public void revert() {
			for (Map.Entry<T,T> r :updatedRows.entrySet()) {
				T left = r.getKey();
				T right = r.getValue() ;
				rows.remove(right); // remove the new
				rows.add(left); // add the old
				events.fireUpdated(right, left);
			}
		}
		
		@Override
		public void commit() {
			for (Map.Entry<T,T> r :updatedRows.entrySet()) {
				T left = r.getKey();
				T right = r.getValue() ;
				rows.remove(left); // remove the new
				rows.add(right); // add the old
				events.fireUpdated(left,right);
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
		
		
			@Override
		public void delete(T row) {
			// check if I need to remove the row from inserted AND updated
				if (getInsertOperation().contains(row))
					getInsertOperation().remove(row);
				if (getUpdateOperation().containsOld(row) || getUpdateOperation().containsNew(row) )
					getUpdateOperation().remove(row);
				
				super.delete(row);
		}

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
	    	//internals.fireDeleted(row);
			events.fireDeleted(row);
		}



		public void fireInserted(T row) {
			//internals.fireInserted(row);
			events.fireInserted(row);
		}

		public void fireDrop(Table<T> table) {
			//internals.fireDrop(table);
			events.fireDrop(table);
		}
	    
		// ##########################################################################
		// UNDO END
		// ##########################################################################

	
}
