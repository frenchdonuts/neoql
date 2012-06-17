package net.ericaro.neoql;





public class Singleton<T> {

	PropertyListenerSupport<T>	support	= new PropertyListenerSupport<T>();
	T							value;
	private TableData<T>		source;
	private TableListener<T>	listener;
	SingletonChange<T> 			singletonChange = null;

	Singleton(TableData<T> source) {
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
		source.addInternalTableListener(listener);
	}
	
	public void drop() {
		this.source.removeInternalTableListener(listener);
		set(null); // also nullify the value
	}

	
	void set(T newValue) {
		T oldValue = value;
		if (singletonChange == null)
			singletonChange = new MySingletonChange();
		singletonChange.set(oldValue, newValue);
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
	
	
	class MySingletonChange extends SingletonChange<T>{
		
		@Override
		public void commit() {
			value = newValue;
			support.fireUpdated(oldValue, newValue);
		}

		@Override
		public void revert() {
			value = oldValue;
			support.fireUpdated(newValue, oldValue);
		}
	}


	public Class<T> getType() {
		return source.getType();
	}
}
