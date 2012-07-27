package net.ericaro.neoql.eventsupport;

import java.util.EventListener;

import net.ericaro.neoql.changeset.Change;

public interface TransactionListener extends EventListener{

	
	/** called when a commit has happened
	 * 
	 * @param change
	 */
	void committed(Change change);
	
	/** the change has not been made, but was rollbacked instead.
	 * 
	 * @param change
	 */
	void rolledBack(Change change);
	
}
