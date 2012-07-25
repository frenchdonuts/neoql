package net.ericaro.neoql.properties;

import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.eventsupport.PropertyListener;
import net.ericaro.neoql.eventsupport.PropertyListenerSupport;
import net.ericaro.neoql.tables.Mapper;

public class MappedProperty<T, C> implements Property<C> {

	PropertyListenerSupport<C>	support	= new PropertyListenerSupport<C>();
	private Property<T>			source;
	private PropertyListener<T>	listener;
	private Mapper<T, C>		map;
	private Class<C>			type;

	MappedProperty(Property<T> source, Class<C> target, Mapper<T, C> map) {
		super();
		this.source = source;
		this.map = map;
		this.type = target;
		listener = new PropertyListener<T>() {
			@Override
			public void updated(T oldValue, T newValue) {
				follow(oldValue, newValue);
			}
		};
		source.addPropertyListener(listener);
	}

	@Override
	public void drop() {
		this.source.removePropertyListener(listener);
		follow(source.get(), null); // also nullify the value
	}

	void follow(T oldValue, T newValue) {
		C oldColumn = map.map(oldValue);
		C newColumn = map.map(newValue);
		if (NeoQL.eq(oldColumn, newColumn))
			support.fireUpdated(oldColumn, newColumn);
	}

	@Override
	public C get() {
		return map.map(source.get());
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
		return type;
	}
}
