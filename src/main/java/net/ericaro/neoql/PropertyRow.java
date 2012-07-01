package net.ericaro.neoql;

import net.ericaro.neoql.changeset.PropertyChange;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;


public class PropertyRow<T> implements Property<T>{

	PropertyListenerSupport<T>	support	= new PropertyListenerSupport<T>();
	T							value;
	private ContentTable<T>		source;
	private TableListener<T>	listener;
	PropertyChange<T> 			propertyChange = null;

	PropertyRow(ContentTable<T> source) {
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
	
	@Override
	public void drop() {
		this.source.removeInternalTableListener(listener);
		set(null); // also nullify the value
	}

	
	void set(T newValue) {
		T oldValue = value;
		if (propertyChange == null)
			propertyChange = new MyPropertyChange();
		propertyChange.set(oldValue, newValue);
	}
	
	@Override
	public T get() {
		return value;
	}

	@Override
	public void addPropertyListener(PropertyListener<T> l) {
		support.addPropertyListener(l);
	}

	@Override
	public void removePropertyListener(PropertyListener<T> l) {
		support.removePropertyListener(l);
	}
	
	
	class MyPropertyChange extends PropertyChange<T>{
		
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


	@Override
	public Class<T> getType() {
		return source.getType();
	}
}
