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

	Setter<T,?>[] setters = new Setter[0];
	Class<T> table;
	private Where<? super T> where;

	Update(Class<T> table) {
		super();
		this.table = table;
	}

	public <U> Update<T> set(Column<U> col, U value) {
		setters = Arrays.copyOf(setters, setters.length+1);
		setters[setters.length-1] = new Setter<T,U>(table, col, value) ;
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

	Setter<T,?>[] getSetters() {
		return setters;
	}

	Class<T> getTable() {
		return table;
	}

	Where<? super T> getWhere() {
		return where;
	}
	
	

}
