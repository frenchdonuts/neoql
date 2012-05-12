package net.ericaro.osql.system;

public interface ColumnListener<V> {

	public void columnUpdated(V oldValue, V newValue);
	
}
