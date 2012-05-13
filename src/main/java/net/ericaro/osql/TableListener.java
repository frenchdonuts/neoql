package net.ericaro.osql;

import java.util.EventListener;

public interface TableListener<T> extends EventListener{

	
	public void updated(T oldRow, T newRow);
	public void deleted(T oldRow);
	public void inserted(T newRow);
	
}
