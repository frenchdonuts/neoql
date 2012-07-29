package net.ericaro.neoql.properties;

import java.lang.ref.WeakReference;

import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Table;
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

	public ObservableCursor(Table<T> source, T val) {
		super();
		this.source = source;
		this.value = val;
		this.listener = new TableListener<T>() {

			private WeakReference<T>	deletedValue;
			@Override
			public void updated(T oldRow, T newRow) {
				if (oldRow == value)
					follow(newRow);

			}

			@Override
			public void deleted(T oldRow) {
				if (oldRow == value) {
					deletedValue = new WeakReference<T>(value) ; // keep a weak ref, to "restore" the tracker if needed
					follow(null);
				}
			}

			@Override
			public void inserted(T newRow) {
				if (value == null && deletedValue !=null && deletedValue.get() == newRow ) {
					// halleluia, it is resurected (probably a nice "undo" manager ;-)
					deletedValue = null;
					follow(newRow);
				}
			}
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
		follow(null); // also nullify the value
	}

	void follow(T newValue) {
		// changed, but this not due to a human decision to move the cursor, but due to the fact that the target has changed
		T oldValue = value;
		value = newValue;
		if (!NeoQL.eq(newValue, oldValue))
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
