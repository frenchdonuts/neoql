package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.ericaro.neoql.eventsupport.AbstractTableListener;
import net.ericaro.neoql.eventsupport.TableListener;
import net.ericaro.neoql.eventsupport.TableListenerSupport;
import net.ericaro.neoql.patches.Delete;
import net.ericaro.neoql.patches.Insert;
import net.ericaro.neoql.patches.PatchBuilder;
import net.ericaro.neoql.patches.Update;

/**
 * basic table, the only table that actually contains data.
 * 
 * @author eric
 * 
 */
public class ContentTable<T> implements Table<T>{

	// public events
	private TableListenerSupport<T>	events			= new TableListenerSupport<T>();
	// internal ones, means between tables through foreign keys only

	private TableListenerSupport<T>	internals		= new TableListenerSupport<T>();	// fire the internal cascading ( i.e foreign key manager)

	Column<T, ?>[]					columns;											// column definition
	private Set<T>					rows			= new HashSet<T>();				// content definition (note that duplicates are not allowed)

	private Class<T>				type;												// table type
	private Database				owner;												// database owner

	private TableListener[]			internalColumnListeners;							// if this table has foreign key (one per column) this are the listeners to the foreign table


	ContentTable(Database owner, Class<T> table, Column[] cols) {
		this.owner = owner;
		this.type = table;

		// copy columns, to force the right type, because I known that Column are of MyColumn type. the interface
		// is just a clan way to store colums.
		assert columnsAreAllOfTheRightType(cols) : "Columns cannot be implemented by third party, they must created with NeoQL.column factory";
		this.columns = new Column[cols.length];
		System.arraycopy(cols, 0, columns, 0, cols.length);
		this.internalColumnListeners = new TableListener[this.columns.length];
	}

	/**
	 * internal assertion methods
	 * 
	 * @param cols
	 * @return
	 */
	private boolean columnsAreAllOfTheRightType(Column[] cols) {
		for (Column c : cols)
			if (!Column.class.isAssignableFrom(c.getClass()))
				return false;
		return true;
	}

	void drop() {
		// disconnect foreign keys
		int i = 0;
		for (Column<T, ?> col : columns)
			disconnectForeignKey(i++, col);
		// if (internals.getListenerCount() > 0)
		// throw new NeoQLException("Cannot drop table " + type + ". Constraint violation(s)" + internals);
		this.rows.clear(); // really ?
		fireDrop(this);
	}

	/**
	 * called by the database (at creation) to connect foreign keys.
	 * 
	 */
	void install() {
		int i = 0;
		for (Column<T, ?> col : columns)
			connectForeignKey(i++, col);
	}

	private <V> void connectForeignKey(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			ContentTable<V> ftable = owner.getTable(col.getType());
			internalColumnListeners[i] = new ForeignKeyColumnListener<V>(col);
			owner.addInternalTableListener(ftable, internalColumnListeners[i]);
		}
	}

	private <V> void disconnectForeignKey(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			ContentTable<V> ftable = owner.getTable(col.getType());
			owner.removeInternalTableListener(ftable, internalColumnListeners[i]);
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

		private Column<T, V>	col;

		ForeignKeyColumnListener(Column<T, V> col) {
			super();
			this.col = col;
		}

		@Override
		public void updated(final V oldValue, final V newValue) {
			// the foreign key has been updated
			if (NeoQL.eq(oldValue, newValue))
				return; // it's a false change, take a shortcut ( when is that happenning, infact ?)

			// now we are going to update every row that points to the oldValue
			// (kind of execute update where vol is oldValue)
			Predicate<T> p = col.is(oldValue);
			ColumnSetter<T, ?>[] setters = new ColumnSetter[] { new ColumnSetter<T, V>(col, newValue) };
			update(p, setters);
		}

		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = col.is(oldValue);
			for (T t : NeoQL.select(ContentTable.this, inUse))
				throw new NeoQLException("Foreign Key violation" + col);
		}

		@Override
		public void dropped(Table<V> table) {
			drop();
		}

		@Override
		public String toString() {
			return "Foreign Key:" + type + " → " + col.getType() + ".";
		}
	}

	// ##########################################################################
	// GETTER BEGIN
	// ##########################################################################
	Database getOwner() {
		return owner;
	}

	/**
	 * Table type, the type of row.
	 * 
	 * @return
	 */
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
	// UPDATE BEGIN
	// ##########################################################################

	T update(T oldValue, ColumnSetter<T, ?>[] setters) {
		T newValue;

		// fix point algorithm, an updated row can trigger internal events that cause it self to change.
		// so we keep the map of old ->new so that the new one is created once, and edited subsequently

		// it won't scale up if we have an already huge transition.

		if (getCurrentCommit().containsNew( oldValue)) // the the oldvalue is a new value, this means that the value as already been updated
			newValue = oldValue;
		else
			newValue = ContentTable.this.clone(oldValue);

		boolean changed = false;
		for (ColumnSetter s : setters)
			changed |= s.set(newValue);
		// TODO handle FK (one of the column might be a FK, and the newValue might not belong to the database

		if (changed) {
			getCurrentCommit().update(type, oldValue, newValue);
			// fire internal events so that other rows might want to keep in touch
			internals.fireUpdated(oldValue, newValue);
		}
		return newValue;
	}

	private PatchBuilder getCurrentCommit() {
		return owner.currentCommit;
	}
	
	T update(T oldValue, T newValue) {
		T clone = clone(newValue);
		getCurrentCommit().update(type, oldValue, clone);
		// fire internal events so that other rows might want to keep in touch
		internals.fireUpdated(oldValue, clone);
		
		return clone;
	}
	
	void update(Predicate<T> p, T t) {
		for (T row : newRowsWhere(p))
			update(row, t);
	}

	void update(Predicate<T> p, ColumnSetter<T, ?>[] setters) {
		for (T row : newRowsWhere(p))
			update(row, setters);

	}

	// ##########################################################################
	// UPDATE END
	// ##########################################################################

	// ##########################################################################
	// DELETE BEGIN
	// ##########################################################################

	/**
	 * delete operation, fill the delete Operation accordingly
	 * 
	 * @param where
	 */
	void delete(Predicate<? super T> where) {
		for (Iterator<T> i = rows.iterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row))
				delete(row);
		}
	}

	private void delete(T row) {
		// check if I need to remove the row from inserted AND updated
		getCurrentCommit().delete(type, row);
		internals.fireDeleted(row);
	}

	// ##########################################################################
	// DELETE END
	// ##########################################################################

	// ##########################################################################
	// INSERT BEGIN
	// ##########################################################################

	T insert(T row) {
		getCurrentCommit().insert(type, row);
		internals.fireInserted(row);
		return row;
	}

	// ##########################################################################
	// INSERT END
	// ##########################################################################

	/**
	 * creates an instance suitable for this table data, with the given columns set
	 * 
	 * @param data
	 * @param values
	 * @return
	 */
	T newInstance(ColumnSetter<T, ?>... values) {

		try {
			T row = type.newInstance();
			assert allSameType(values) : "Column Setter mismatch: cannot use alien column setter";
			for (ColumnSetter s : values)
				s.set(row);
			// TODO : handle FK verification here, one of the column might be a FK, hence I need to check that the value belongs to the database
			// TODO handle default values (like ids etc, so I need to 'update' the object a little bit and return it

			return row;
		} catch (Exception e) {
			throw new NeoQLException("Exception while instanciating row for table " + type, e);
		}
	}

	T clone(T row) {
		T clone = newInstance();
		for (Column<T, ?> c : columns)
			c.copy(row, clone);
		return clone;
	}

	// helper method to assert that all columnvalue have the same class definition
	private boolean allSameType(ColumnSetter<T, ?>... values) {
		if (values.length == 0)
			return true;
		for (ColumnSetter cv : values)
			if (!type.isAssignableFrom(cv.column.getTable()))
				return false;
		return true;
	}

	/**
	 * special query that include the transaction content.
	 * 
	 * @return
	 */
	Iterable<T> newRowsWhere(Predicate<T> p) {
		Set<T> updated = new HashSet<T>();

		for (T row : rows)
			// not rows but the virtual new rows
			if (p.eval(row) // matches the predicate
					&& !getCurrentCommit().containsOld(row) // but it has not changed
				)
				updated.add(row); // old plain value
		// now handle updated
		for (Object row : getCurrentCommit().newValues())
			// also need to update the 'new' ones
			
			if (type.isInstance(row) && p.eval((T)row)) // match, and it has not changed
				updated.add((T)row);
		// note that updated should not contains deleted (the operation should have been checked before
		return Collections.unmodifiableCollection(updated);
	}

	// ##########################################################################
	// ACTUAL TRANSACTION OPERATIONS BEGIN
	// ##########################################################################

	void doCommit(Update<T> p) {
			rows.remove(p.getOldValue()); // remove the new
			rows.add(p.getNewValue()); // add the old
			events.fireUpdated(p.getOldValue(), p.getNewValue());
	}

	public void doCommit(Insert<T> p) {
			if (rows.add(p.getInserted()))// actually add the item back
				fireInserted(p.getInserted());
	}

	void doCommit(Delete<T> p) {
			if (rows.remove(p.getDeleted()))// actually add the item back
				fireDeleted(p.getDeleted());
	}

	// ##########################################################################
	// ACTUAL TRANSACTION OPERATIONS END
	// ##########################################################################

	// ##########################################################################
	// OPERATIONS BEGIN
	// ##########################################################################

	void fireDeleted(T row) {
		events.fireDeleted(row);
	}

	void fireInserted(T row) {
		events.fireInserted(row);
	}

	void fireDrop(Table<T> table) {
		events.fireDrop(table);
	}

	// ##########################################################################
	// UNDO END
	// ##########################################################################
}
