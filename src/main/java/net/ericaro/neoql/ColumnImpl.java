package net.ericaro.neoql;

/**
 * An Abstract column handles the foreign table definition, and some EDSL implementations.
 * 
 * @author eric
 * 
 * @param <T>
 * @param <V>
 */
class ColumnImpl<T, V> implements Column<T, V> {

	// the foreign class definition, null if there is no foreign class defeintion.
	private ClassTableDef<V>	foreignTable;
	Attribute<T, V>				attr;

	public ColumnImpl(Attribute<T, V> attr, ClassTableDef<V> foreignTable) {
		super();
		this.attr = attr;
		this.foreignTable = foreignTable;
	}

	public ColumnImpl(Attribute<T, V> attr) {
		this(attr, null);
	}

	/**
	 * Copies every columns values from src into target.
	 * 
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

	public ClassTableDef<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return foreignTable != null;
	}

	void init(Class<T> tableClass) {}

	public Predicate<T> is(final V value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false
				return value.equals(ColumnImpl.this.get(t));
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

		};
	}

}
