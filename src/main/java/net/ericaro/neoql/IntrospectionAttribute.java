package net.ericaro.neoql;

import java.lang.reflect.Field;


public class IntrospectionAttribute<T, V> implements Attribute<T, V> {

	Field field;
	String fname;

	IntrospectionAttribute(Class<T> tableClass, String fname) {
		super();
		this.fname = fname;
		init(tableClass);
	}

	public void set(T src, V value) {
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
