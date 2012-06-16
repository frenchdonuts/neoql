package net.ericaro.neoql;

import java.util.Arrays;


/**
 * Basic table def. This is the only table that can be edited
 * 
 * @author eric
 * 
 * @param <T>
 */
public class ClassTableDef<T>  {

	Class<T>			table;		// the type class
	ColumnDef<T, ?>[]	columns;	// type type columns

	/**
	 * Creates an empty classtable def, a builder pattern would have been more
	 * suitable
	 * 
	 * @param table
	 */
	public ClassTableDef(Class<T> table) {
		super();
		this.table = table;
		this.columns = new ColumnDef[0];
	}

	/**
	 * add a column by introspecting the fields.
	 * 
	 * @param name
	 *            the field name
	 * @return
	 */
	public <V> Column<T, V> addColumn(String name) {
		return addColumn(name, null);
	}

	/**
	 * add a Column by introspecting the fields
	 * 
	 * @param name
	 * @param foreignKey
	 * @return
	 */
	public <V> Column<T, V> addColumn(String name, ClassTableDef<V> foreignKey) {
		IntrospectionAttribute<T, V> attr = new IntrospectionAttribute<T, V>(table, name);
		return addColumn(attr, foreignKey);
	}

	
		
	/**
	 * Add a column by using the attribute accessor
	 * 
	 * @param attr
	 * @param foreignKey
	 * @return
	 */
	public <V> Column<T, V> addColumn(Attribute<T, V> attr, ClassTableDef<V> foreignKey) {
		ColumnDef<T, V> c = new ColumnDef<T, V>(this, attr, foreignKey);
		columns = Arrays.copyOf(columns, columns.length + 1);
		columns[columns.length - 1] = c;
		return c;
	}

	/**
	 * return the table def class.
	 * 
	 * @return
	 */
	public Class<T> getTable() {
		return table;
	}

	public Column<T, ?>[] getColumns() {
		return columns;
	}

	
	
	
	/**
	 * return the identity predicate for this type.
	 * 
	 * @param value
	 * @return
	 */
	public Predicate<T> is(final T value) {
		return NeoQL.is(value);
	}

	public String getName() {
		return table.getSimpleName();
	}

	@Override
	public String toString() {
		return getName();
	}

	public String toTableDefinition() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName()).append("(");
		for (Column c : columns)
			sb.append(c).append(",");
		sb.append(")");
		return sb.toString();
	}

}
