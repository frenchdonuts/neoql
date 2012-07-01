package net.ericaro.neoql;

public interface Singleton<T> {

	public abstract Class<T> getType();

	public abstract void removePropertyListener(PropertyListener<T> l);

	public abstract void addPropertyListener(PropertyListener<T> l);

	/** retrieve the current value pointed by this singleton.
	 * 
	 * @return
	 */
	public abstract T get();

	public abstract void drop();

}
