package net.ericaro.osql.system;

import javax.swing.event.EventListenerList;

public class TableListenerSupport<T> {

	EventListenerList listeners = new EventListenerList();

	public TableListenerSupport() {
		super();
	}

	public void addTableListener(TableListener<T> l) {
		listeners.add(TableListener.class, l);
	}

	public void removeTableListener(TableListener<T> l) {
		listeners.remove(TableListener.class, l);
	}

	private TableListener<T>[] listeners() {
		return listeners.getListeners(TableListener.class);
	}

	public void fireDeleted(T row) {
		for (TableListener<T> l : listeners())
			l.deleted(row);
	}

	public void fireInserted(T row) {
		for (TableListener<T> l : listeners())
			l.inserted(row);
	}

	public void fireUpdated(T oldRow, T newRow) {
		for (TableListener<T> l : listeners())
			l.updated(oldRow, newRow);
	}
}