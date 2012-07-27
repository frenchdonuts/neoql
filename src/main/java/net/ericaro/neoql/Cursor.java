package net.ericaro.neoql;

import java.lang.ref.WeakReference;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;

/** A cursor is an observable property that follows an single row in a table.
 * Whenever it changes it fires events to notify observers. There are two sources for changes:
 * the source "row" is changing, the cursor fires a simple event.
 * the database is editing (through the "moveTo" method) the cursor, then the change is store in a change, and applied in the next commit.
 * 
 * @author eric
 *
 * @param <T>
 */
public class Cursor<T> implements Property<T>, Content {

	PropertyListenerSupport<T>	support			= new PropertyListenerSupport<T>();
	T							value;
	private Table<T>			source;
	private TableListener<T>	listener;
	PropertyChange<T>			propertyChange	= null;
	private Object	key;

	/** creates a new cursor, key is any object used to retrieve the cursor later, must be unique per database
	 * 
	 * @param key
	 * @param source
	 */
	Cursor(Object key, Table<T> source) {
		super();
		this.key = key;
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

	public Object getKey() {
		return key;
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
		if (!NeoQL.eq(newValue, oldValue))
			support.fireUpdated(oldValue, newValue);
	}
	
	/** changed due to a user's decision to point to something else, the change is "transactional" like any other changes.
	 * 
	 * @param newValue
	 */
	void set(T newValue) {
		T oldValue = value;
		if (propertyChange == null)
			propertyChange = new PropertyChange<T>(key);
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

	@Override
	public Class<T> getType() {
		return source.getType();
	}
	
	void doCommit(PropertyChange<T> change) {
		support.fireUpdated(value, value=change.getNewValue());
	}

	@Override
	public void accept(ContentVisitor visitor) { visitor.visit(this);}
	
	
	
}
