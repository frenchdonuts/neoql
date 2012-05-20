package net.ericaro.neoql;


public abstract class AbstractColumn<T, V> implements Column<T,V> {



	ClassTableDef<V> foreignTable;
	
	AbstractColumn() {
		super();
	}

	AbstractColumn(ClassTableDef<V> foreignTable) {
		this();
		this.foreignTable = foreignTable;
	}
	
	 void copy(T src, T target) {
		set(target, get(src));
	}

	abstract void set(T src, V value);
	

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

	void init(Class<T> tableClass)  {}

	 public Predicate<T> is(final V value) {
			return new Predicate<T>() {

				@Override
				public boolean eval(T t) {
					if (value == null)
						return false; // null is always false
					return value.equals(AbstractColumn.this.get(t));
				}

			};
		}
	 
	 /** if this columns has a foreign key, returns a predicate that is true if the pair left joins.
	  * for instance
	  * for a Pair<Student,Teacher> p, and this column is "Student.teacher" then
	  * p.getLeft().teacher = p.getRight()
	  * 
	  * 
	  * @return
	  */
	 public Predicate<Pair<T,V>> joins() {
			return new Predicate<Pair<T,V>>() {

				@Override
				public boolean eval(Pair<T,V> t) {
					return get(t.getLeft()) == t.getRight() ;
				}

			};
		}

	
}
