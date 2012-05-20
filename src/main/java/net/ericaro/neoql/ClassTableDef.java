package net.ericaro.neoql;

import java.util.Arrays;
import java.util.Iterator;


public class ClassTableDef<T> implements TableDef<T> {

	Class<T> table;
	private Column<T, ?>[] columns;

	 public ClassTableDef(Class<T> table) {
		super();
		this.table = table;
		this.columns = new Column[0];
	}
	
	 public <V> Column<T,V> addColumn(String name){
		 return addColumn(name, null);
	 }

	 public <V> Column<T,V> addColumn(String name, ClassTableDef<V> foreignKey ){
		 Column<T, V> c = new Column<T, V>(name, foreignKey);
		 columns = Arrays.copyOf(columns, columns.length+1);
		 columns[columns.length-1] = c;
		 c.init(table);
		 return c;
	 }
	
	public Class<T> getTable() {
		return table;
	}

	public Column<T, ?>[] getColumns() {
		return columns;
	}
	
	@Override
	public TableData<T> asTable(Database database) {
		return database.tableFor(this);
	}

	@Override
	public Iterator<T> iterator(final Database database) {
				return database.tableFor(this).iterator();
	}

	public T newInstance() {
	try {
		return table.newInstance();
	} catch (Exception e) {
		throw new NeoQLException(
				"Exception while instanciating row for table " + table, e);
	}
	}
	
	public Predicate<T> is(final T value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false, is it ?
				return t == value ;
			}

		};
	}
	
	

}
