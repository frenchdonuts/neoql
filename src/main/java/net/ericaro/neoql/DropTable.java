package net.ericaro.neoql;


/** a Drop Table Statement
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

	 Class<T> getTable() {
		return table;
	}

	 void setTable(Class<T> table) {
		this.table = table;
	}

	@Override
	 public void executeOn(Database database) {
		database.execute(this);
	}
	
	
	
	
}
