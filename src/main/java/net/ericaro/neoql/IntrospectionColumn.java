package net.ericaro.neoql;

import java.lang.reflect.Field;


public class IntrospectionColumn<T, V> extends AbstractColumn<T,V> implements Mapper<T, V> {

	// that's part of the 
	Field field;
	String fname;

	IntrospectionColumn(String fname) {
		super();
		this.fname = fname;
	}

	IntrospectionColumn(String fname, ClassTableDef<V> foreignTable) {
		super(foreignTable);
		this.fname = fname;
	}

	
	

	void set(T src, V value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
	}

	 public V get(T src) {
		try {
			return (V) field.get(src);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
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
}
