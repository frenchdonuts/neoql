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

	TableListenerSupport<T>	events	 = new TableListenerSupport<T>();
	TableListenerSupport<T>	internals= new TableListenerSupport<T>(); // fire the internal cascading ( i.e foreign key manager)

	Column<T, ?>[]			table;
	List<T>					rows	= new ArrayList<T>();
	private Class<T>		type;
	private Database		owner;

	private static <T> Column<T, ?>[] columnsOf(Class<T> tableClass) {
		List<Column<T, ?>> cols = new ArrayList<Column<T, ?>>();
		try {
			
			for (Field f : tableClass.getDeclaredFields()) {
				int mod = f.getModifiers(); 
				if ( Modifier.isStatic(mod) && Modifier.isPublic(mod) && f.get(null) instanceof Column) {
					// I should write stuff in the col object, it will be reused
					Column<T, ?> col = (Column<T, ?>) f.get(null);
					col.init(tableClass);
					cols.add(col);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cols.toArray(new Column[cols.size()]);
	}

	 TableData(Database owner, Class<T> metadata) {
		this.owner = owner;
		this.type = metadata;
		this.table = columnsOf(metadata);

	}

	void install() {
		for (Column<T, ?> col : table)
			installColumn(col);
	}

	private <V> void installColumn(Column<T, V> col) {
		if (col.hasForeignKey())
			owner.addInternalTableListener(col.getForeignTable(), new ForeignKeyColumnListener<V>(col));
	}

	public void addTableListener(TableListener<T> listener) {
		events.addTableListener(listener);
	}

	@Override
	public  void removeTableListener(TableListener<T> listener) {
		events.removeTableListener(listener);
	}
	
	 void addInternalTableListener(TableListener<T> listener) {
		internals.addTableListener(listener);
	}
	
	 void removeInternalTableListener(TableListener<T> listener) {
		internals.removeTableListener(listener);
	}

	class ForeignKeyColumnListener<V> implements TableListener<V> {

		private Column<T, V>	col;

		 ForeignKeyColumnListener(Column<T, V> col) {
			super();
			this.col = col;
		}

		@Override
		public  void updated(V oldValue, V newValue) {
			if (oldValue == newValue)
				return;
			Update<T> update = new Update<T>(type, NeoQL.is(col, oldValue), new ColumnValuePair<T, V>(col, newValue));
			owner.execute(update);
		}

		@Override
		public  void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = NeoQL.is(col, oldValue);
			for (T t : owner.select(type, inUse))
				throw new NeoQLException("Foreign Key violation" + col);
		}

		@Override
		public  void inserted(V newRow) {}

	}

	Database getOwner() {
		return owner;
	}

	@Override
	public  Iterator<T> iterator() {
		return rows.iterator();
	}

	T append(T row) {
		rows.add(row);// todo do some check here
		return row;
	}

	 T clone(T row) {
		try {
			T clone = type.newInstance();
			for (Column<T, ?> c : table)
				c.copy(row, clone);
			return clone;
		} catch (Exception e) {
			throw new RuntimeException("unexpected exception while cloning", e);
		}

	}

	Map<T, T>	updatedRows	= new HashMap<T, T>();
	int			reintrant	= -1;

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
				for (Entry<T,T> e : updatedRows.entrySet())
					events.fireUpdated(e.getValue(), e.getKey());
				updatedRows.clear();
			} else
				System.out.println("reintrant update: do not clean the updated set");

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
