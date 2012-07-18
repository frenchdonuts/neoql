package net.ericaro.neoql;

/**
 * A property implementation that tracks a final instance. Yes, its silly, but sometimes it makes implementation
 * cleaner to be able to handle trivial cases. Tracking something that cannot change its trivial.
 * 
 * @author eric
 * 
 * @param <T>
 */
public class PropertyValue<T> implements Property<T> {

	private Class<T>	type;
	private final T		value;

	public PropertyValue(T value) {
		this((Class<T>) value.getClass(), value);
	}
	
	public PropertyValue(Class<T> type, T value) {
		super();
		this.value = value;
		this.type = type;
	}

	@Override
	public Class<T> getType() {
		return type;
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {}

	@Override
	public void addPropertyListener(PropertyListener<T> l) {}

	@Override
	public T get() {
		return value;
	}

	@Override
	public void drop() {}

}
