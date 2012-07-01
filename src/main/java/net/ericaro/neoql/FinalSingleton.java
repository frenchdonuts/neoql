package net.ericaro.neoql;

/**
 * A singleton implementation that tracks a final instance. Yes, its silly, but sometimes it makes implementation
 * cleaner to be able to handle trivial cases. Tracking something that cannot change its trivial.
 * 
 * @author eric
 * 
 * @param <T>
 */
public class FinalSingleton<T> implements Singleton<T> {

	private Class<T>	type;
	private final T		value;

	public FinalSingleton(T value) {
		super();
		this.value = value;
		this.type = (Class<T>) value.getClass();
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
