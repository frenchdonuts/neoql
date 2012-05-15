package net.ericaro.osql;


/** a Create Table Statement
 * 
 * @author eric
 *
 * @param <T>
 */
 public class CreateTable<T> implements Statement {

	private Class<T> table;

	 CreateTable(Class<T> table) {
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
