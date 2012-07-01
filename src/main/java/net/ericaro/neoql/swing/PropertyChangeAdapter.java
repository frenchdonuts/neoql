package net.ericaro.neoql.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import net.ericaro.neoql.PropertyListener;
import net.ericaro.neoql.Property;

public class PropertyChangeAdapter<T> implements PropertyListener<T> {
	
	Object source;
	String propertyName;
	PropertyChangeListener listener;

	public PropertyChangeAdapter(Object source, String propertyName, PropertyChangeListener listener) {
		super();
		this.source = source;
		this.propertyName = propertyName;
		this.listener = listener;
	}

	@Override
	public void updated(T oldValue, T newValue) {
		listener.propertyChange(new PropertyChangeEvent(source,propertyName, oldValue, newValue));
	}
	
	
	
	
}
