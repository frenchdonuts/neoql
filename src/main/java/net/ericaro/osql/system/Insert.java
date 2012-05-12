package net.ericaro.osql.system;

import java.util.Arrays;
import java.util.Iterator;


/** INSERT INTO ( Table) <setters>
 * 
 * @author eric
 *
 */
public class Insert<T> implements Operation<Void>{

	
	Setter<T,?>[] setters = new Setter[0];
	Class<T> table;
	T row;
	
	Insert() {
		super();
	}
	public Insert(Class<T> table) {
		this.table = table;
	}
	public <U> Insert<T> set(Column<T,U> col, U value) {
		setters = Arrays.copyOf(setters, setters.length+1);
		setters[setters.length-1] = new Setter<T,U>(table, col, value) ;
		return this;
	}

	public T build() {
		try {
			row = table.newInstance();
			for (Setter<T, ?> s : setters)
				s.set(row);
		} catch (Exception e) {
			throw new DQLException("Exception while instanciating row for table "+table,e);
		}
		return row;
		
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
	public T getRow() {
		if (row == null)
			build();
		return row;
	}

	
	
	
	
	
	
	
}
