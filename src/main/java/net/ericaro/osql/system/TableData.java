package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * basic table,
 * essentially a metadata and an iterable of Object[]
 * 
 * @author eric
 * 
 */
public class TableData<T> implements Table<T> {

	Column<T,?>[]			table;
	List<T>				rows	= new ArrayList<T>();
	private Class<T>	type;
	private Database	owner;

	public TableData(Database owner, Class<T> metadata) {
		this.owner = owner;
		this.type = metadata;
		this.table = DQL.columnsOf(metadata);
		for(Column<T,?> col: table)
			installColumn(col);
	}
	
	private <V> void installColumn(Column<T,V> col){
		if (col.hasForeignKey() )
			owner.addDatabaseListener(col.getForeignTable(), new ForeignKeyColumnListener<V>(col) );
	}
	
	class ForeignKeyColumnListener<V> implements DatabaseListener<V>{
	
	public void addDatabaseListener(DatabaseListener<T> listener) {
		owner.addDatabaseListener(type, listener);
	}

		
		@Override
		public void updated(V oldValue, V newValue) {
			TableData.this.owner.update(type).set(col, newValue).where(DQL.columnIs(col, oldValue));
		}


		@Override
		public void deleted(V oldValue) {
			// fire an exception ( forbidding the deleting if the value is in use ?
			Where<T> inUse = DQL.columnIs(col, oldValue);
			for(T t: rows)
				if (inUse.isTrue(t))
					throw new DQLException("Foreign Key violation"+ col);
		}
		@Override
		public void inserted(V newRow) {}
		
		
	
	}

	Database getOwner() {
		return owner;
	}



	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}

	public ListIterator<T> listIterator() {
		return rows.listIterator();
	}

	public ListIterator<T> listIterator(int index) {
		return rows.listIterator(index);
	}

	T append(T row) {
		rows.add(row);// todo do some check here
		return row;
	}
	
	public T clone(T row) {
		try {
			T clone = type.newInstance();
			for (Column<T,?> c : table)
				c.copy(row, clone);
			return clone;
		} catch (Exception e) {
			throw new RuntimeException("unexpected exception while cloning", e);
		}

	}

	/*
	 * replace the row by its clone, so that the update can happen, and the immutability is safe
	 */
	public void update(T row, T newValue) {
		int i = rows.indexOf(row);
		rows.set(i, newValue);
	}

}
