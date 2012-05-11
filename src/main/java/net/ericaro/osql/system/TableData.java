package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** basic table, 
 *  essentially a metadata and an iterable of Object[]
 * @author eric
 *
 */
public class TableData<T> implements Iterable<T>{

	Column[] table;
	List<T> rows = new ArrayList<T>() ;
	private Class<T> type;
	
	public TableData(Class<T> metadata) {
		this.type = metadata;
		this.table = DQL.columnsOf(metadata);
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
	
}
