package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.Statement;

 class PropertyValue<T> implements Statement{

	 Property<T> property;
	T value;

	PropertyValue(Property<T> property, T value) {
		super();
		this.property = property;
		this.value = value;
	}

	Property<T> getProperty() {
		return property;
	}

	T getValue() {
		return value;
	}

	@Override
	public void executeOn(Database database) {
		database.put(property, value);
	}
	
	
	
}
