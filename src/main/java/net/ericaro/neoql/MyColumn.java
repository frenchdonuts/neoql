package net.ericaro.neoql;

import net.ericaro.neoql.tables.Pair;


/**
 * The column implementation. Separated from the interface to avoid references to it in the client's model.
 * @author eric
 * 
 * @param <T>
 * @param <V>
 */
class MyColumn<T, V> implements Column<T, V> {

	private Class<V>	foreignTable; // column's type, it can also be the foreign table
	Attribute<T, V>				attr;
	private Class<T>	table;
	private final boolean hasForeignKey;

	public MyColumn(Class<T> table,  Attribute<T, V> attr, Class<V> foreignTable, boolean hasForeignKey) {
		super();
		this.table = table;
		this.attr = attr;
		this.foreignTable = foreignTable;
		this.hasForeignKey = hasForeignKey;
	}

	
	@Override
	public ColumnSetter<T, V> set(Singleton<V> value) {
		return set(value.get());
	}
	@Override
	public ColumnSetter<T, V> set(V value) {
		return new ColumnSetter<T, V>( this, value);
	}

	public Class<T> getTable(){ return table;}
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

	@Override
	public V get(T src) {
		return attr.get(src);
	}
	boolean set(T src, V value) {
		if (NeoQL.eq(value , get(src)) )
			return false;
		attr.set(src, value);
		return true;
	}

	@Override
	public V map(T source) {
		return get(source);
	}

	public Class<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return hasForeignKey;
	}

	
	public Predicate<T> is(final Singleton<V> value) {
		return new Predicate<T>() {
				public boolean eval(T t) {
					V that = MyColumn.this.get(t);
					V v = value.get();
					return NeoQL.eq(v, that);
				}
				public String toString() {
					return MyColumn.this.attr + " = " + value;
				}
			};
	}

	
	public Predicate<T> is(final V value) {
		return new Predicate<T>() {
				public boolean eval(T t) {
					V that = MyColumn.this.get(t);
					if (value == null)
						return that == null; // null is always false
					else
						return value.equals(that);
				}
				public String toString() {
					return MyColumn.this.attr + " = " + value;
				}

			};
	}


	
	private Class<V> getType() {
		return attr.getType();
	}
	@Override
	public String toString() {
		return attr+" "+getType().getSimpleName()+(foreignTable==null?"":" FOREIGN KEY REFERENCES "+foreignTable.getName());
	}


	
	
	
}
