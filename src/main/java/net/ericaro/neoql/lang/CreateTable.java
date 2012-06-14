package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Statement;
import net.ericaro.neoql.system.TableDef;



/**
 * a Create Table Statement: SQL like
 * CREATE TABLE
 * 
 * @author eric
 * 
 * @param <T>
 */
public class CreateTable<T> implements Statement {

	private TableDef<T> tableDef;
	
	
	CreateTable(TableDef<T> tableDef) {
		super();
		this.tableDef = tableDef;
	}

	public TableDef<T> getTableDef() {
		return tableDef;
	}
	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

	@Override
	public String toString() {
		if (tableDef instanceof ClassTableDef)
			return "CREATE TABLE " + ((ClassTableDef)tableDef).toTableDefinition() + ";";
		else
			return "CREATE TABLE (" + tableDef + ");";
	}
	
	

}
