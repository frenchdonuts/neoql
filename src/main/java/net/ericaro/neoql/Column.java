package net.ericaro.neoql;

import java.lang.reflect.Field;


public class Column<T, V> implements Mapper<T, V> {

	ClassTableDef<V> foreignTable;

	// that's part of the 
	Field field;
	String fname;

	Column(String fname) {
		super();
		this.fname = fname;
	}

	Column(String fname, ClassTableDef<V> foreignTable) {
		this(fname);
		this.foreignTable = foreignTable;
	}

	
	
	 void copy(T src, T target) {
		set(target, get(src));
	}

	void set(T src, V value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
	}

	@Override
	public V map(T source) {
		return get(source);
	}

	 V get(Object src) {
		try {
			return (V) field.get(src);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
	}

	public ClassTableDef<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return foreignTable != null;
	}

	void init(Class<T> tableClass)  {
		if (field == null) {// not init
			try {
				field = tableClass.getDeclaredField(fname);
				field.setAccessible(true);
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("Introspection Error initializing column "+fname, e);
			}
		}
	}

	 public Predicate<T> is(final V value) {
			return new Predicate<T>() {

				@Override
				public boolean eval(T t) {
					if (value == null)
						return false; // null is always false
					return value.equals(Column.this.get(t));
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
