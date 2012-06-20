package net.ericaro.neoql;

public class ColumnSingleton<T,V> implements Singleton<T> {

	private Column<V,T>	col;
	PropertyListenerSupport<T>	support	= new PropertyListenerSupport<T>();
	
	public ColumnSingleton(Singleton<V> row, Column<V,T> column) {
		this.col = column;
		row.addPropertyListener(new PropertyListener<V>() {
			@Override
			public void updated(V oldValue, V newValue) {
				T oldCol = col.get(oldValue);
				T newCol = col.get(newValue);
				if (oldCol != newCol)
					support.fireUpdated(oldCol, newCol);
					
					
			}
			
		});
	}

	public Class<T> getType() {
		return null;
	}

	public void removePropertyListener(PropertyListener<T> l) {}

	public void addPropertyListener(PropertyListener<T> l) {}

	public T get() {
		return null;
	}

	public void drop() {}

	
	
	
}
