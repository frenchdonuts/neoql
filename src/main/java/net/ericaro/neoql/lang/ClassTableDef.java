package net.ericaro.neoql.lang;

import java.util.Arrays;
import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.system.Attribute;
import net.ericaro.neoql.system.Column;
import net.ericaro.neoql.system.NeoQLException;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.TableDef;

/**
 * Basic table def. This is the only table that can be edited
 * 
 * @author eric
 * 
 * @param <T>
 */
public class ClassTableDef<T> implements TableDef<T> {

	private Class<T>			table;		// the type class
	private ColumnDef<T, ?>[]	columns;	// type type columns

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
		ColumnDef<T, V> c = new ColumnDef<T, V>(attr, foreignKey);
		columns = Arrays.copyOf(columns, columns.length + 1);
		columns[columns.length - 1] = c;
		return c;
	}

	/**
	 * return the table def class.
	 * 
	 * @return
	 */
	Class<T> getTable() {
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

	/**
	 * creates a new instance of T
	 * 
	 * @return
	 */
	public T newInstance() {
		try {
			return table.newInstance();
		} catch (Exception e) {
			throw new NeoQLException("Exception while instanciating row for table " + table, e);
		}
	}

	public T clone(T row) {
		T clone = newInstance();
		for (ColumnDef<T, ?> c : columns)
			c.copy(row, clone);
		return clone;
	}	
	
	void insertInto(Database db, InsertInto<T> i) {
		T row = newInstance();
		for (ColumnValue s : i.getColumnValuePairs()) {
			ColumnDef c = (ColumnDef) s.getColumn();
			c.set(row, s.getValue());
		}
	}
	
	/**
	 * return the identity predicate for this type.
	 * 
	 * @param value
	 * @return
	 */
	public Predicate<T> is(final T value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false, is it ?
				return t == value;
			}
		};
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
