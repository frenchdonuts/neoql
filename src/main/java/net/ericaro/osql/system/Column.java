package net.ericaro.osql.system;

import java.lang.reflect.Field;

public class Column<T> {

	public Field field;
	
	void copy(Object src, Object target ) {
		set(target, get(src) );
	}
	
	protected void set(Object src, T value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}
	protected T get(Object src) {
		try {
			return (T) field.get(src);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}
	
	
}
