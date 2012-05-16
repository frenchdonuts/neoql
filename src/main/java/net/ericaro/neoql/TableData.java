package net.ericaro.neoql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
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
class TableData<T> implements Table<T> {

	TableListenerSupport<T> events = new TableListenerSupport<T>();
	TableListenerSupport<T> internals = new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)

	Column<T, ?>[] columns;
	List<T> rows = new ArrayList<T>();
	private Class<T> type;
	private Database owner;
	private TableListener[] columnListeners;

	

	TableData(Database owner, CreateTable<T> create) {
		this.owner = owner;
		this.type = create.getTable();
		this.columns = create.getColumns();
		this.columnListeners = new TableListener[this.columns.length];

	}

	
	
	@Override
	public void drop(Database from) {
		from.drop(type);
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
			throw new NeoQLException("Cannot drop table " + type.getName()
					+ ". Constraint violation(s)" + internals);

	}

	private <V> void installColumn(int i, Column<T, V> col) {
		if (col.hasForeignKey()) {
			ForeignKeyColumnListener<V> listener = new ForeignKeyColumnListener<V>(
					col);
			columnListeners[i] = listener;
			owner.addInternalTableListener(col.getForeignTable(), listener);
		}
	}

	private <V> void unInstallColumn(int i, Column<T, V> col) {
		if (col.hasForeignKey())
			owner.removeInternalTableListener(col.getForeignTable(),
					columnListeners[i]);
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

	class ForeignKeyColumnListener<V> implements TableListener<V> {

		private Column<T, V> col;

		ForeignKeyColumnListener(Column<T, V> col) {
			super();
			this.col = col;
		}

		@Override
		public void updated(V oldValue, V newValue) {
			if (oldValue == newValue)
				return;
			Update<T> update = new Update<T>(type, NeoQL.is(col, oldValue),
					new ColumnValuePair<T, V>(col, newValue));
			owner.execute(update);
		}

		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = NeoQL.is(col, oldValue);
			for (T t : owner.select(type, inUse))
				throw new NeoQLException("Foreign Key violation" + col);
		}

		@Override
		public void inserted(V newRow) {}

		@Override
		public String toString() {
			return "Foreign Key:" + type.getName() + "." + col.fname + " → "
					+ col.foreignTable.getName() + ".";
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
		try {
			T clone = type.newInstance();
			for (Column<T, ?> c : columns)
				c.copy(row, clone);
			return clone;
		} catch (Exception e) {
			throw new RuntimeException("unexpected exception while cloning", e);
		}

	}

	Map<T, T> updatedRows = new HashMap<T, T>();
	int reintrant = -1;

	void update(T row, ColumnValuePair<T, ?>[] setters) {
		reintrant++;
		try {
			T clone;
			if (updatedRows.containsKey(row))
				clone = row;
			else {
				clone = clone(row);
				updatedRows.put(clone, row);
			}

			for (ColumnValuePair s : setters) {
				s.column.set(clone, s.value);
			}

			int i = rows.indexOf(row);
			rows.set(i, clone);
			internals.fireUpdated(row, clone); // fire updates only once per session

		} finally {
			reintrant--;
			if (reintrant < 0) {
				for (Entry<T, T> e : updatedRows.entrySet())
					events.fireUpdated(e.getValue(), e.getKey());
				updatedRows.clear();
			} else
				System.out
						.println("reintrant update: do not clean the updated set");

		}

	}

	private boolean contains(T value) {
		return rows.contains(value);
	}

	void delete(Predicate<? super T> where) {
		for (ListIterator<T> i = rows.listIterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row)) {
				i.remove();
				internals.fireDeleted(row);
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

}
