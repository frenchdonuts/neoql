package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;

/**
 * a Drop Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DropTable<T> implements Statement {

	private Class<T> table;

	DropTable(Class<T> table) {
		super();
		this.table = table;
	}

	public Class<T> getTable() {
		return table;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
