package net.ericaro.neoql.system;

import javax.swing.event.EventListenerList;

public class PropertyListenerSupport<T> {

	EventListenerList listeners = new EventListenerList();

	public PropertyListenerSupport() {
		super();
	}

	public void addPropertyListener(PropertyListener<T> l) {
		listeners.add(PropertyListener.class, l);
	}

	public void removePropertyListener(PropertyListener<T> l) {
		listeners.remove(PropertyListener.class, l);
	}

	private PropertyListener<T>[] listeners() {
		return listeners.getListeners(PropertyListener.class);
	}

	public int getListenerCount() {
		return listeners.getListenerCount(PropertyListener.class);
	}

	public void fireUpdated(T oldRow, T newRow) {
		for (PropertyListener<T> l : listeners())
			l.updated(oldRow, newRow);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		for (PropertyListener<T> l : listeners())
			sb.append(l.toString()).append("\n");
			return sb.toString();
	}
	 
	 
}