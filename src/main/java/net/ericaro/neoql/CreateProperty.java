package net.ericaro.neoql;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * a Create Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class CreateProperty<T> implements Statement {

	private Class<T> table;
	private Property<T> property;
	
	CreateProperty(Class<T> table, Property<T> property) {
		super();
		this.table = table;
		this.property = property;
	}

	public Class<T> getTable() {
		return table;
	}

	public Property<T> getProperty() {
		return property;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
