package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.TableListener;



public interface Table<T> extends Iterable<T> {

	void addTableListener(TableListener<T> listener);
	void removeTableListener(TableListener<T> listener);
	
	/** this tables drops every thing.
	 * 
	 */
	//public void drop();
	
}
