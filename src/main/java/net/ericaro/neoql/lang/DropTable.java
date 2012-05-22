package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Statement;


/**
 * a Drop Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DropTable<T> implements Statement {

	private ClassTableDef<T> table;

	DropTable(ClassTableDef<T> table) {
		super();
		this.table = table;
	}

	public ClassTableDef<T> getTable() {
		return table;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

	@Override
	public String toString() {
		return "DROP TABLE " + table.getName() ;
	}

	
}
