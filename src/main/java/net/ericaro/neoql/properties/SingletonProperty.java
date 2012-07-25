package net.ericaro.neoql.properties;

import net.ericaro.neoql.Property;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.PropertyChange;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;

/**
 * A Singleton implementation.
 * @author eric
 * 
 * @param <T>
 */
public class SingletonProperty<T> implements Property<T> {

	PropertyListenerSupport<T>	support			= new PropertyListenerSupport<T>();
	private Class<T>			type;
	private T					value;
	PropertyChange<T>			propertyChange	= null;

	SingletonProperty(Class<T> type) {
		super();
		this.type = type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public void addPropertyListener(PropertyListener<T> l) {
		support.addPropertyListener(l);
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {
		support.removePropertyListener(l);
	}

	@Override
	public T get() {
		return value;
	}

	void set(T newValue) {
		T oldValue = value;
		if (propertyChange == null)
			propertyChange = new MyPropertyChange();
		propertyChange.set(oldValue, newValue);
	}

	@Override
	public void drop() {}

	class MyPropertyChange extends PropertyChange<T> {

		@Override
		public Change copy() {
			MyPropertyChange that = new MyPropertyChange();
			that.newValue = this.newValue;
			that.oldValue = this.newValue;
			return that;
		}

		@Override
		public void commit() {
			value = newValue;
			support.fireUpdated(oldValue, newValue);
		}

		@Override
		public void revert() {
			value = oldValue;
			support.fireUpdated(newValue, oldValue);
		}
	}

}
