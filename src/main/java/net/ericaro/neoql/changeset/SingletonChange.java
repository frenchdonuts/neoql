package net.ericaro.neoql.changeset;


public abstract class SingletonChange<T> implements Change {
	protected T oldValue, newValue;
	
	public void set(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue ;
	}
	
	
}
