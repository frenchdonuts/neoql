package net.ericaro.osql.system;

public interface Table<T> extends Iterable<T> {

	void addDatabaseListener(DatabaseListener<T> listener);
	void removeDatabaseListener(DatabaseListener<T> listener);
	
}
