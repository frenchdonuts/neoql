package net.ericaro.neoql;

public abstract class AbstractTableListener<T> implements TableListener<T> {

	@Override
	public void updated(T oldRow, T newRow) {}

	@Override
	public void deleted(T oldRow) {}

	@Override
	public void inserted(T newRow) {}

}
