package net.ericaro.neoql.eventsupport;

import java.util.EventListener;

import net.ericaro.neoql.Table;

public interface TableListener<T> extends EventListener{

	
	public void updated(T oldRow, T newRow);
	public void deleted(T oldRow);
	public void inserted(T newRow);
	public void dropped(Table<T> table);
	
}
