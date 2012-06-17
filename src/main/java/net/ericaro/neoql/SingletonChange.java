package net.ericaro.neoql;

public abstract class SingletonChange<T> implements Change {
	T oldValue, newValue;
	
	public void set(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue ;
	}
	
	
}
