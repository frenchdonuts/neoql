package net.ericaro.neoql.eventsupport;

import net.ericaro.neoql.Table;


/** dev/null listener for TableListener. Make it simpler to override a single method
 * 
 * @author eric
 *
 * @param <T>
 */
public abstract class AbstractTableListener<T> implements TableListener<T> {

	@Override
	public void updated(T oldRow, T newRow) {}

	@Override
	public void deleted(T oldRow) {}

	@Override
	public void inserted(T newRow) {}
	
	public void dropped(Table<T> table) {}

}
