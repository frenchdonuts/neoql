package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.Statement;


/**
 * A Drop a property statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DropProperty<T> implements Statement {

	private Property<T> property;

	DropProperty(Property<T> property) {
		super();
		this.property = property;
	}

	public Property<T> getProperty() {
		return property;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
