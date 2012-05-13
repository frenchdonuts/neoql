package net.ericaro.osql.system;

public interface Table<T> extends Iterable<T> {

	void addTableListener(TableListener<T> listener);
	void removeTableListener(TableListener<T> listener);
	
}
