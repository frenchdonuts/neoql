package net.ericaro.neoql;

import java.lang.reflect.Field;

/**
 * Implementation of attribute that uses introspection to get/set the attribute.
 * 
 * @author eric
 * 
 * @param <T>
 * @param <V>
 */
public class IntrospectionAttribute<T, V> implements Attribute<T, V> {

	private Field	field;

	public IntrospectionAttribute(Field field) {
		super();
		this.field = field;
		field.setAccessible(true);
	}

	protected IntrospectionAttribute(Class<T> tableClass, String fname) {
		super();
		try {
			field = tableClass.getDeclaredField(fname);
			field.setAccessible(true);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Introspection Error initializing column " + fname, e);
		}
	}

	@Override
	public Class<V> getType() {
		return (Class<V>) field.getType();
	}

	public void set(T src, V value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
	}

	public V get(T src) {
		if (src == null)
			return null;
		try {
			return (V) field.get(src);
		} catch (Exception e) {
			throw new RuntimeException("wrong field", e);
		}
	}

	@Override
	public String toString() {
		return field.getName();
	}

}
