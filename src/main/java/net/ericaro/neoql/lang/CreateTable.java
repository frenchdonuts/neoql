package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Statement;



/**
 * a Create Table Statement: SQL like
 * CREATE TABLE
 * 
 * @author eric
 * 
 * @param <T>
 */
public class CreateTable<T> implements Statement {

	private ClassTableDef<T> tableDef;
	
	
	CreateTable(ClassTableDef<T> tableDef) {
		super();
		this.tableDef = tableDef;
	}

	public ClassTableDef<T> getTableDef() {
		return tableDef;
	}
	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

	@Override
	public String toString() {
		return "CREATE TABLE " + tableDef.toTableDefinition() + ";";
	}
	
	

}
