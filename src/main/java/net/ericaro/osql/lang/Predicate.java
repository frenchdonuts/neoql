package net.ericaro.osql.lang;

public interface Predicate<T> {

	public boolean eval(T t);
	
}
