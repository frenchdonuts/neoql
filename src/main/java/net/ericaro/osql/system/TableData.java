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
public class TableData<T> implements Iterable<T> {

	Column[]			table;
	List<T>				rows	= new ArrayList<T>();
	private Class<T>	type;
	private Database	owner;

	public TableData(Database owner, Class<T> metadata) {
		this.owner = owner;
		this.type = metadata;
		this.table = DQL.columnsOf(metadata);
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

	T newRow() {
		try {
			return append(type.newInstance());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public T clone(T row) {
		try {
			T clone = type.newInstance();
			for (Column<?> c : table)
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
