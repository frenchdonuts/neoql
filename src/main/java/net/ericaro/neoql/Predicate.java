package net.ericaro.neoql;

public interface Predicate<T> {

	public boolean eval(T t);
	
}
