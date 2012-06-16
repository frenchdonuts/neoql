package net.ericaro.neoql;

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

	public int getListenerCount() {
		return listeners.getListenerCount(TableListener.class);
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
	 public void fireDrop(Table<T> table) {
		 for (TableListener<T> l : listeners())
			 l.dropped(table);
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