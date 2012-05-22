package net.ericaro.neoql.system;

public interface Predicate<T> {

	public boolean eval(T t);
	
}
