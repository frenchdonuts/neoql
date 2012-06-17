package net.ericaro.neoql;





public class Singleton<T> {

	PropertyListenerSupport<T>	support	= new PropertyListenerSupport<T>();
	T							value;
	private TableData<T>		source;
	private TableListener<T>	listener;

	public Singleton(TableData<T> source) {
		super();
		this.source = source;
		this.listener = new TableListener<T>() {

			@Override
			public void updated(T oldRow, T newRow) {
				if (oldRow == value)
					set(newRow);

			}

			@Override
			public void deleted(T oldRow) {
				if (oldRow == value) // ? delete or not delete ?
					set(null);
			}

			@Override
			public void inserted(T newRow) {}
			
			@Override
			public void dropped(Table<T> table) {
				drop();
			}

		};
		source.addTableListener(listener);
	}
	
	public void drop() {
		this.source.removeTableListener(listener);
		set(null); // also nullify the value
	}

	
	void set(T newValue) {
		T old = value;
		value = newValue;
		support.fireUpdated(old, newValue);
	}

	public T get() {
		return value;
	}

	public void addPropertyListener(PropertyListener<T> l) {
		support.addPropertyListener(l);
	}

	public void removePropertyListener(PropertyListener<T> l) {
		support.removePropertyListener(l);
	}

	public void dropTable() {
		source.removeTableListener(listener);
	}

}
