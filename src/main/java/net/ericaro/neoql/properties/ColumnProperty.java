package net.ericaro.neoql.properties;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.ContentTable;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.PropertyChange;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.eventsupport.TableListener;

public class ColumnProperty<T, C> implements Property<C> {

	PropertyListenerSupport<C>	support	= new PropertyListenerSupport<C>();
	private Property<T>		source;
	private PropertyListener<T>	listener;
	private Column<T, C>		col;

	ColumnProperty(Property<T> source, Column<T, C> col) {
		super();
		this.source = source;
		this.col = col;
		listener = new PropertyListener<T>() {
			@Override
			public void updated(T oldValue, T newValue) {
				follow(oldValue, newValue);
			}};
		source.addPropertyListener(listener);
	}

	@Override
	public void drop() {
		this.source.removePropertyListener(listener);
		follow(source.get(),null); // also nullify the value
	}
	void follow(T oldValue, T newValue) {
		C oldColumn = col.get(oldValue);
		C newColumn = col.get(newValue);
		if (!NeoQL.eq(oldColumn, newColumn))
			support.fireUpdated(oldColumn, newColumn);
	}

	@Override
	public C get() {
		return col.get(source.get());
	}

	@Override
	public void addPropertyListener(PropertyListener<C> l) {
		support.addPropertyListener(l);
	}

	@Override
	public void removePropertyListener(PropertyListener<C> l) {
		support.removePropertyListener(l);
	}

	@Override
	public Class<C> getType() {
		return col.getType();
	}
}
