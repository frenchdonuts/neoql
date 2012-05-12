package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Callbacks {

	Map<Class, List> listeners = new HashMap<Class, List>();
	Map<Column, List> clisteners = new HashMap<Column, List>();

	protected <T> List<DatabaseListener<T>> listenersFor(Class<T> table) {
		List<DatabaseListener<T>> list = (List<DatabaseListener<T>>) listeners
				.get(table);
		if (list == null) {
			list = new ArrayList<DatabaseListener<T>>();
			listeners.put(table, list);
		}
		return list;

	}

	protected <T,V> List<ColumnListener<V>> listenersFor(Column<T,V> column) {
		List<ColumnListener<V>> list = (List<ColumnListener<V>>) clisteners
				.get(column);
		if (list == null) {
			list = new ArrayList<ColumnListener<V>>();
			clisteners.put(column, list);
		}
		return list;

	}

	public <T> void addDatabaseListener(Class<T> table,
			DatabaseListener<T> listener) {
		listenersFor(table).add(listener);
	}

	public <T> void removeDatabaseListener(Class<T> table, DatabaseListener<T> listener) {
		listenersFor(table).remove(listener);
	}

	public <T,V> void addColumnListener(Column<T,V> column,
			ColumnListener<V> listener) {
		listenersFor(column).add(listener);
	}

	public <T,V> void removeColumnListener(Column<T,V> column, ColumnListener<V> listener) {
		listenersFor(column).remove(listener); 
	}

	
	public void transactionBegun() {
		System.out.println("transactionBegun");
	}

	public void transactionCommitted() {
		System.out.println("transactionCommitted");
	}

	public <T> void tableCreated(Class<T> table) {
		System.out.println("tableCreated");
	}

	public <T> void rowInserted(Class<T> table, T row) {
		for (DatabaseListener<T> listener : listenersFor(table))
			listener.inserted(row);
	}

	public <T> void rowUpdated(Class<T> table, T before, T row) {
		for (DatabaseListener<T> listener : listenersFor(table))
			listener.updated(before, row);
	}

	public <T> void rowDeleted(Class<T> table, T row) {
		for (DatabaseListener<T> listener : listenersFor(table))
			listener.deleted(row);
	}

	public <T, V> void columnUpdated(Setter<T, V> s, T oldRow) {
		Column<T,V> column = s.getColumn();
		List<ColumnListener<V>> listeners = listenersFor(column);
		if (listeners.size() > 0) {
			V oldValue = s.getColumn().get(oldRow);
			V newValue = s.getValue();
			for (ColumnListener<V> listener : listeners)
				listener.columnUpdated(oldValue, newValue);
		}
	}

}
