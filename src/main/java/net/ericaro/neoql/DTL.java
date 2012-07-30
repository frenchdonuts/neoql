package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.TransactionListener;
import net.ericaro.neoql.patches.Patch;


/** Data Transaction Language Interface
 * 
 * @author eric
 *
 */
public interface DTL {
	Patch commit();
	Patch rollback();
	void addTransactionListener(TransactionListener l);
	void removeTransactionListener(TransactionListener l);
	public abstract void setAutocommit(boolean autocommit);
	public abstract boolean isAutocommit();
}
