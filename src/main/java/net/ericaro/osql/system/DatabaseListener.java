package net.ericaro.osql.system;

import java.util.EventListener;

public interface DatabaseListener<T> extends EventListener{

	
	public void updated(T oldRow, T newRow);
	public void deleted(T oldRow);
	public void inserted(T newRow);
	
}
