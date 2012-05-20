package net.ericaro.neoql;

import java.util.Arrays;
import java.util.Iterator;


public class ClassTableDef<T> implements TableDef<T> {

	Class<T> table;
	private AbstractColumn<T, ?>[] columns;

	 public ClassTableDef(Class<T> table) {
		super();
		this.table = table;
		this.columns = new AbstractColumn[0];
	}
	
	 /** add a column by introspecting the fields.
	  * 
	  * @param name
	  * @return
	  */
	 public <V> AbstractColumn<T,V> addColumn(String name){
		 return addColumn(name, null);
	 }
	 /** add a Column by introspecting the fields
	  * 
	  * @param name
	  * @param foreignKey
	  * @return
	  */
	 public <V> AbstractColumn<T,V> addColumn(String name, ClassTableDef<V> foreignKey ){
		 return doAddCol(new IntrospectionColumn<T, V>(name, foreignKey));
	 }

	 
	 public <V> AbstractColumn<T,V> addColumn(Attribute<T,V> attr, ClassTableDef<V> foreignKey ){
		 return doAddCol(new AttributeColumn<T,V>(attr, foreignKey));
	 }
	 
	 
	 private <V> AbstractColumn<T, V> doAddCol(AbstractColumn<T, V> c) {
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
