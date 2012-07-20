package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.PropertyListenerSupport;

/** A property that tracks a single column value from a row property
 * 
 * @author eric
 *
 * @param <T> Table type
 * @param <C> Column type
 */
public class PropertyColumn<T,C> implements Property<C> {

	private Column<T,C>	col;
	private PropertyListenerSupport<C>	support	= new PropertyListenerSupport<C>();
	private Property<T> row;
	private PropertyListener<T> listener;
	
	PropertyColumn(Property<T> row, Column<T,C> column) {
		this.col = column;
		this.row = row;
		listener = new PropertyListener<T>() {
			@Override
			public void updated(T oldRow, T newRow) { // row has changed, track changes for the column itself
				C oldCol = col.get(oldRow); 
				C newCol = col.get(newRow);
				if (!NeoQL.eq(oldCol, newCol))
					support.fireUpdated(oldCol, newCol);
			}
		};
		row.addPropertyListener(listener);
	}

	/** return the column's type
	 * 
	 */
	public Class<C> getType() {
		return col.getType();
	}

	public void removePropertyListener(PropertyListener<C> l) {support.removePropertyListener(l);}

	public void addPropertyListener(PropertyListener<C> l) {support.addPropertyListener(l);}

	

	public C get() {
		return col.get(row.get());
	}

	public void drop() {
		row.removePropertyListener(listener);
	}

	
	Property<T> getRow() {
		return row;
	}

	Column<T,C> getColumn() {
		return col;
	}


	
	
	
}