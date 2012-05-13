package net.ericaro.osql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.EventListenerList;


/**
 * basic table, essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class TableData<T> implements Table<T> {

	TableListenerSupport<T> events = new TableListenerSupport<T>();
	
	Column<T, ?>[] table;
	List<T> rows = new ArrayList<T>();
	private Class<T> type;
	private Database owner;

	private static <T> Column<T, ?>[] columnsOf(Class<T> tableClass) {
		List<Column<T, ?>> cols = new ArrayList<Column<T, ?>>();
		try {
			for (Field f : tableClass.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) && Modifier.isStatic(mod)
						&& Modifier.isPublic(mod)
						&& f.get(null) instanceof Column) {
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

	public TableData(Database owner, Class<T> metadata) {
		this.owner = owner;
		this.type = metadata;
		this.table = columnsOf(metadata);
		for (Column<T, ?> col : table)
			installColumn(col);
	}

	private <V> void installColumn(Column<T, V> col) {
		if (col.hasForeignKey())
			owner.addTableListener(col.getForeignTable(),
					new ForeignKeyColumnListener<V>(col));
	}

	public void addTableListener(TableListener<T> listener) {
		events.addTableListener(listener);
	}

	@Override
	public void removeTableListener(TableListener<T> listener) {
		events.removeTableListener(listener);
	}

	class ForeignKeyColumnListener<V> implements TableListener<V> {

		private Column<T, V> col;

		public ForeignKeyColumnListener(Column<T, V> col) {
			super();
			this.col = col;
		}

		@Override
		public void updated(V oldValue, V newValue) {
			Update<T> update = new Update<T>(type, DQL.columnIs(col, oldValue), new ColumnValuePair<T, V>(col, newValue));
			owner.execute(update);
		}

		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in
			// use ?
			Predicate<T> inUse = DQL.columnIs(col, oldValue);
			Select<T> select = new Select<T>(type, inUse);
			for (T t: owner.select(select) )
					throw new DQLException("Foreign Key violation" + col);
		}

		@Override
		public void inserted(V newRow) {
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

	public T clone(T row) {
		try {
			T clone = type.newInstance();
			for (Column<T, ?> c : table)
				c.copy(row, clone);
			return clone;
		} catch (Exception e) {
			throw new RuntimeException("unexpected exception while cloning", e);
		}

	}

	/*
	 * replace the row by its clone, so that the update can happen, and the
	 * immutability is safe
	 */
	public void update(T row, T newValue) {
		//TODO if there is a foerign key check that it actually exists in its row
		// otherwise fail to update
		int i = rows.indexOf(row);
		rows.set(i, newValue);
		events.fireUpdated(row, newValue);
	}

	public void delete(Predicate<? super T> where) {
		for (ListIterator<T> i = rows.listIterator(); i.hasNext();) {
			T row = i.next();
			if (where.eval(row)) {
				i.remove();
				events.fireDeleted(row);
			}
		}
	}
	
	public T insert(T row) {
		rows.add(row);
		events.fireInserted(row);
		return row;
	}
	
}
