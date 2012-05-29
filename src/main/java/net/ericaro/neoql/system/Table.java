package net.ericaro.neoql.system;

import net.ericaro.neoql.Database;


public interface Table<T> extends Iterable<T> {

	void addTableListener(TableListener<T> listener);
	void removeTableListener(TableListener<T> listener);
	
	public void drop(Database from);
}
