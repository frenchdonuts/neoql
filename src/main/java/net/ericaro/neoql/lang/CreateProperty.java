package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.Statement;



/**
 * a Create Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class CreateProperty<T> implements Statement {

	private ClassTableDef<T> table;
	private Property<T> property;
	
	CreateProperty(ClassTableDef<T> table, Property<T> property) {
		super();
		this.table = table;
		this.property = property;
	}

	public ClassTableDef<T> getTable() {
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