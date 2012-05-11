package net.ericaro.osql.system;

import java.util.Arrays;
import java.util.Iterator;


/** INSERT INTO ( Table) <setters>
 * 
 * @author eric
 *
 */
public class Insert<T> implements Operation<Void>{

	
	Setter[] setters = new Setter[0];
	Class<T> table;
	
	Insert() {
		super();
	}
	public Insert(Class<T> table) {
		this.table = table;
	}
	public <U> Insert<T> set(Column<U> col, U value) {
		setters = Arrays.copyOf(setters, setters.length+1);
		setters[setters.length-1] = new Setter(table, col, value) ;
		return this;
	}

	@Override
	public Void run(Database database) {
		database.run(this);
		return null;
	}
	Setter[] getSetters() {
		return setters;
	}
	Class<T> getTable() {
		return table;
	}

	
	
	
	
	
	
	
}
