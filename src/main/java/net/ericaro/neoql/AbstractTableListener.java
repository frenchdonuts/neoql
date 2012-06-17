package net.ericaro.neoql;


/** dev/null listener for TableListener. Make it simpler to override a single method
 * 
 * @author eric
 *
 * @param <T>
 */
abstract class AbstractTableListener<T> implements TableListener<T> {

	@Override
	public void updated(T oldRow, T newRow) {}

	@Override
	public void deleted(T oldRow) {}

	@Override
	public void inserted(T newRow) {}
	
	public void dropped(Table<T> table) {}

}
