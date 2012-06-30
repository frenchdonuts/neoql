package net.ericaro.neoql;

import net.ericaro.neoql.eventsupport.PropertyListenerSupport;

/** A singleton that tracks a single column value from a row singleton
 * 
 * @author eric
 *
 * @param <T>
 * @param <V>
 */
public class ColumnSingleton<T,V> implements Singleton<V> {

	private Column<T,V>	col;
	private PropertyListenerSupport<V>	support	= new PropertyListenerSupport<V>();
	private Singleton<T> row;
	private PropertyListener<T> listener;
	
	public ColumnSingleton(Singleton<T> row, Column<T,V> column) {
		this.col = column;
		this.row = row;
		listener = new PropertyListener<T>() {
			@Override
			public void updated(T oldValue, T newValue) {
				V oldCol = oldValue==null?null: col.get(oldValue);
				V newCol = newValue==null?null: col.get(newValue);
				if (oldCol != newCol)
					support.fireUpdated(oldCol, newCol);
					
					
			}
			
		};
		row.addPropertyListener(listener);
	}

	public Class<V> getType() {
		return col.getForeignTable();
	}

	public void removePropertyListener(PropertyListener<V> l) {support.removePropertyListener(l);}

	public void addPropertyListener(PropertyListener<V> l) {support.addPropertyListener(l);}

	public V get() {
		return col.get(row.get());
	}

	public void drop() {
		row.removePropertyListener(listener);
	}

	Singleton<T> getRow() {
		return row;
	}

	Column<T,V> getColumn() {
		return col;
	}


	
	
	
}
