package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.PropertyListener;

public interface Property<T> {

	public abstract Class<T> getType();

	public abstract void removePropertyListener(PropertyListener<T> l);

	public abstract void addPropertyListener(PropertyListener<T> l);

	/** retrieve the current value pointed by this property.
	 * 
	 * @return
	 */
	public abstract T get();

	public abstract void drop();

}
