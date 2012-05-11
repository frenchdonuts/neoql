package net.ericaro.osql.system;

public interface Predicate {

	
	boolean eval(Object[] row);
	
}
