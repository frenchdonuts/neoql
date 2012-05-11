package net.ericaro.osql.system;

import java.util.Arrays;
import java.util.Iterator;


/**
 * INSERT INTO ( Table) <setters>
 * 
 * @author eric
 * 
 */
public class Update<T> implements Operation<Void> {

	Setter[] setters = new Setter[0];
	Class<T> table;
	private Where<? super T> where;

	Update(Class<T> table) {
		super();
		this.table = table;
	}

	public Update<T> set(Column col, Object value) {
		setters = Arrays.copyOf(setters, setters.length+1);
		setters[setters.length-1] = new Setter(table, col, value) ;
		return this;
	}

	public Update<T> where(Where<? super T> p) {
		this.where = p;
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

	Class getTable() {
		return table;
	}

	Where<? super T> getWhere() {
		return where;
	}
	
	

}
