package net.ericaro.osql.lang;

import net.ericaro.osql.system.Database;

/** a Create Table Statement
 * 
 * @author eric
 *
 * @param <T>
 */
public class CreateTable<T> implements Statement {

	private Class<T> table;

	public CreateTable(Class<T> table) {
		super();
		this.table = table;
	}

	public Class<T> getTable() {
		return table;
	}

	public void setTable(Class<T> table) {
		this.table = table;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
	
	
	
	
}
