package net.ericaro.neoql;


/**
 * The column implementation. Separated from the interface to avoid references to it in the client's model.
 * @author eric
 * 
 * @param <T>
 * @param <V>
 */
class ColumnDef<T, V> implements Column<T, V> {

	// the foreign class definition, null if there is no foreign class definition
	private Class<V>	foreignTable;
	Attribute<T, V>				attr;
	private Class<T>	table;

	public ColumnDef(Class<T> table,  Attribute<T, V> attr, Class<V> foreignTable) {
		super();
		this.table = table;
		this.attr = attr;
		this.foreignTable = foreignTable;
	}

	public ColumnDef(Class<T> table, Attribute<T, V> attr) {
		this(table, attr, null);
	}
	
	@Override
	public ColumnValue<T, V> set(Singleton<V> value) {
		return set(value.get());
	}
	@Override
	public ColumnValue<T, V> set(V value) {
		return new ColumnValue<T, V>( this, value);
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
	void set(T src, V value) {
		attr.set(src, value);
	}

	@Override
	public V map(T source) {
		return get(source);
	}
	

	@Override
	public String getName() {
		return attr.getName();
	}

	public Class<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return foreignTable != null;
	}

	
	public Predicate<T> is(final Singleton<V> value) {
		return new Predicate<T>() {
				public boolean eval(T t) {
					V that = ColumnDef.this.get(t);
					V v = value.get();
					if (v == null)
						return that == null; // null is always false
					else
						return v.equals(that);
				}
				public String toString() {
					return ColumnDef.this.attr.getName() + " = " + value;
				}

			};
	}

	
	public Predicate<T> is(final V value) {
		return new Predicate<T>() {
				public boolean eval(T t) {
					V that = ColumnDef.this.get(t);
					if (value == null)
						return that == null; // null is always false
					else
						return value.equals(that);
				}
				public String toString() {
					return ColumnDef.this.attr.getName() + " = " + value;
				}

			};
	}

	/**
	 * if this columns has a foreign key, returns a predicate that is true if the pair left joins.
	 * for instance
	 * for a Pair<Student,Teacher> p, and this column is "Student.teacher" then
	 * p.getLeft().teacher = p.getRight()
	 * 
	 * 
	 * @return
	 */
	public Predicate<Pair<T, V>> joins() {
		return new Predicate<Pair<T, V>>() {

			@Override
			public boolean eval(Pair<T, V> t) {
				return get(t.getLeft()) == t.getRight();
			}
			public String toString(){
				return ColumnDef.this.attr.getName()+".id = that.id";
			}

		};
	}
	
	private Class<V> getType() {
		return attr.getType();
	}
	@Override
	public String toString() {
		return getName()+" "+getType().getSimpleName()+(foreignTable==null?"":" FOREIGN KEY REFERENCES "+foreignTable.getName());
	}


	
	
	
}
