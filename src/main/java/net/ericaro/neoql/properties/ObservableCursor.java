package net.ericaro.neoql.properties;

import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.changeset.PropertyChange;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;

/** Observable only cursor property
 * 
 * @author eric
 *
 * @param <T>
 */
public class ObservableCursor<T> implements Property<T>{

	PropertyListenerSupport<T>	support	= new PropertyListenerSupport<T>();
	T							value;
	private Table<T>		source;
	private TableListener<T>	listener;
	PropertyChange<T> 			propertyChange = null;

	public ObservableCursor(Table<T> source) {
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
	
	@Override
	public void drop() {
		this.source.removeTableListener(listener);
		set(null); // also nullify the value
	}

	
	void set(T newValue) {
		T oldValue = value;
		value = newValue;
		if (NeoQL.eq(newValue, oldValue))
			support.fireUpdated(oldValue, newValue);
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
	
	


	@Override
	public Class<T> getType() {
		return source.getType();
	}
}
