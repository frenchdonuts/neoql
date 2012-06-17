package net.ericaro.neoql;



public interface Table<T> extends Iterable<T> {

	void addTableListener(TableListener<T> listener);
	void removeTableListener(TableListener<T> listener);
	
	/** this tables drops every thing.
	 * 
	 */
	public void drop();
}
