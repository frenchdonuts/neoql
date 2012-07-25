package net.ericaro.neoql;

import net.ericaro.neoql.properties.FinalProperty;

/**
 * The column implementation. Separated from the interface to avoid references to it in the client's model.
 * 
 * @author eric
 * 
 * @param <T>
 *            table type
 * @param <C>
 *            column type
 */
public class Column<T, C> {

	private final Attribute<T, C>			attr;
	private final Class<T>		table;
	private final boolean	hasForeignKey;

	Column(Class<T> table, Attribute<T, C> attr, Class<C> foreignTable, boolean hasForeignKey) {
		super();
		this.table = table;
		this.attr = attr;
		this.hasForeignKey = hasForeignKey;
	}

	/** 
	 * @param property
	 * @return
	 */
	public ColumnSetter<T, C> set(Property<C> value) {
		return new ColumnSetter<T, C>(this, value);
	}

	/** Returns a ColumnSetter
	  * @param value
	  * @return
	  */
	public ColumnSetter<T, C> set(C value) {
		return new ColumnSetter<T, C>(this, value);
	}

	/** return the table's type.
	 * 
	 */
	public Class<T> getTable() {
		return table;
	}

	/**
	 * Copies every columns values from src into target.
	 * new ColumnValue<T, V>((ColumnDef<T, V>) col, value);
	 * make use of the abstract get and set method to achieve it's goal.
	 * 
	 * @param src
	 * @param target
	 */
	void copy(T src, T target) {
		set(target, get(src));
	}

	/** returns the value of for this column, and src row.
	 * 
	 * @param src
	 * @return
	 */
	public C get(T src) {
		return attr.get(src);
	}

	boolean set(T src, C value) {
		if (NeoQL.eq(value, get(src)))
			return false;
		attr.set(src, value);
		return true;
	}

	C map(T source) {
		return get(source);
	}

	/** returns the class that defines the type associated with this column.
	 * 
	 * @return
	 */
	public Class<C> getType() {
		return attr.getType();
	}

	public boolean hasForeignKey() {
		return hasForeignKey;
	}

	/** return a predicate that test the identity of the given property
	 * 
	 * @param value
	 * @return
	 */
	public Predicate<T> is(final Property<C> value) {
		return new Predicate<T>() {
			public boolean eval(T t) {
				C that = Column.this.get(t);
				C v = value.get();
				return NeoQL.eq(v, that);
			}

			public String toString() {
				return Column.this.attr + " = " + value;
			}
		};
	}

	/** returns a predicate that test the == for this column's value and the value
	 * 
	 * @param value
	 * @return
	 */
	public Predicate<T> is(final C value) {
		return is(new FinalProperty<C>(value));
	}
	
	public Predicate<T> isNull(Class<C> type) {
		return is(new FinalProperty<C>(type, null));
	}

	@Override
	public String toString() {
		return attr + " " + getType().getSimpleName() + (hasForeignKey ? "" : " FOREIGN KEY REFERENCES " + getType().getName());
	}

}
