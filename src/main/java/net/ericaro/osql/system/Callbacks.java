package net.ericaro.osql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Callbacks {

	Map<Class,List> listeners= new HashMap<Class, List>();
	
	
	
	protected <T> List<DatabaseListener<T>> listenersFor(Class<T> table){
		List<DatabaseListener<T>> list = (List<DatabaseListener<T>>) listeners.get(table);
		if (list == null) {
			list = new ArrayList<DatabaseListener<T>>();
			listeners.put(table, list);
		}
		return list;
		
	}
	
	public <T> void addDatabaseListener(Class<T> table, DatabaseListener<T> listener) {
		listenersFor(table).add(listener);
	}
	public void removeDatabaseListener(Class table, DatabaseListener listener) {
		listenersFor(table).remove(listener);
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
		for(DatabaseListener<T> listener: listenersFor(table) )
			listener.inserted(row);
	}

	public <T> void rowUpdated(Class<T> table, T before, T row) {
		for(DatabaseListener<T> listener: listenersFor(table) )
			listener.updated(before, row);
	}

	public <T> void rowDeleted(Class<T> table, T row) {
		for(DatabaseListener<T> listener: listenersFor(table) )
			listener.deleted(row);
	}

}
