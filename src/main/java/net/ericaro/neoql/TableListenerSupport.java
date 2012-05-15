package net.ericaro.neoql;

import javax.swing.event.EventListenerList;

 class TableListenerSupport<T> {

	EventListenerList listeners = new EventListenerList();

	 TableListenerSupport() {
		super();
	}

	 void addTableListener(TableListener<T> l) {
		listeners.add(TableListener.class, l);
	}

	 void removeTableListener(TableListener<T> l) {
		listeners.remove(TableListener.class, l);
	}

	private TableListener<T>[] listeners() {
		return listeners.getListeners(TableListener.class);
	}

	public int getListenerCount() {
		return listeners.getListenerCount(TableListener.class);
	}

	 void fireDeleted(T row) {
		for (TableListener<T> l : listeners())
			l.deleted(row);
	}

	 void fireInserted(T row) {
		for (TableListener<T> l : listeners())
			l.inserted(row);
	}

	 void fireUpdated(T oldRow, T newRow) {
		for (TableListener<T> l : listeners())
			l.updated(oldRow, newRow);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (TableListener<T> l : listeners())
			sb.append(l.toString()).append("\n");
			return sb.toString();
	}
	 
	 
}