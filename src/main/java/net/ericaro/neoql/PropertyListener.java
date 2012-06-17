package net.ericaro.neoql;

import java.util.EventListener;

public interface PropertyListener<T> extends EventListener{
	
	void updated(T oldValue, T newValue);
}
