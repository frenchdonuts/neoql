package net.ericaro.neoql.eventsupport;

import java.util.EventListener;

import net.ericaro.neoql.patches.Patch;

public interface TransactionListener extends EventListener{

	
	/** called when a commit has happened
	 * 
	 * @param change
	 */
	void committed(Patch change);
	
	/** the change has not been made, but was rollbacked instead.
	 * 
	 * @param change
	 */
	void rolledBack(Patch change);
	
}
