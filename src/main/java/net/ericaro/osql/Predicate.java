package net.ericaro.osql;

public interface Predicate<T> {

	public boolean eval(T t);
	
}
