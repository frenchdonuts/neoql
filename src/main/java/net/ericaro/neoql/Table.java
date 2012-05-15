package net.ericaro.neoql;

public interface Table<T> extends Iterable<T> {

	void addTableListener(TableListener<T> listener);
	void removeTableListener(TableListener<T> listener);
	
	public void drop(Database from);
}
