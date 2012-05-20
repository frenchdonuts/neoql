package net.ericaro.neoql;

public interface Attribute<T,V> {

	
	public V get(T object);
	public void set(T object, V value);
}
