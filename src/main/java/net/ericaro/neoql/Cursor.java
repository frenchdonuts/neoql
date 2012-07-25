package net.ericaro.neoql;

import java.lang.ref.WeakReference;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.PropertyChange;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;

public class Cursor<T> implements Property<T> {

	PropertyListenerSupport<T>	support			= new PropertyListenerSupport<T>();
	T							value;
	private Table<T>			source;
	private TableListener<T>	listener;
	PropertyChange<T>			propertyChange	= null;

	Cursor(Table<T> source) {
		super();
		this.source = source;
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
		set(null); // also nullify the value
	}

	void follow(T newValue) {
		// changed, but this not due to a human decision to move the cursor, but due to the fact that the target has changed
		T oldValue = value;
		value = newValue;
		support.fireUpdated(oldValue, newValue);
	}
	
	/** changed due to a user's decision to point to something else, the change is "transactional" like any other changes.
	 * 
	 * @param newValue
	 */
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

	class MyPropertyChange extends PropertyChange<T> {

		@Override
		public Change copy() {
			MyPropertyChange that = new MyPropertyChange();
			that.newValue = this.newValue;
			that.oldValue = this.newValue;
			return that;
		}

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
