package net.ericaro.neoql.eventsupport;

import javax.swing.event.EventListenerList;

import net.ericaro.neoql.changeset.Change;


public class TransactionListenerSupport {

	EventListenerList listeners = new EventListenerList();

	public TransactionListenerSupport() {
		super();
	}

	public void addTransactionListener(TransactionListener l) {
		listeners.add(TransactionListener.class, l);
	}

	public void removeTransactionListener(TransactionListener l) {
		listeners.remove(TransactionListener.class, l);
	}

	private TransactionListener[] listeners() {
		return listeners.getListeners(TransactionListener.class);
	}

	public int getListenerCount() {
		return listeners.getListenerCount(TransactionListener.class);
	}

	public void fireCommitted(Change change) {
		for (TransactionListener l : listeners())
			l.committed(change);
	}
	public void fireRolledBack(Change change) {
		for (TransactionListener l : listeners())
			l.rolledBack(change);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (TransactionListener l : listeners())
			sb.append(l.toString()).append("\n");
			return sb.toString();
	}
}