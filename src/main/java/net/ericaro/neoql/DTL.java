package net.ericaro.neoql;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.TransactionListener;


/** Data Transaction Language Interface
 * 
 * @author eric
 *
 */
public interface DTL {
	Change commit();
	Change rollback();
	void addTransactionListener(TransactionListener l);
	void removeTransactionListener(TransactionListener l);
	public abstract void setAutocommit(boolean autocommit);
	public abstract boolean isAutocommit();
}
