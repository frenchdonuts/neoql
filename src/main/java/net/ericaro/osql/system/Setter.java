package net.ericaro.osql.system;

import java.lang.reflect.Field;

class Setter<T> {

	Object  value ;
	private Field f;
	
	Setter(Class<T> table, Column col, Object value) {
		this.value = value;
		f = col.field;
	}
	
	
	void set(T row) {
		try {
			f.set(row, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}

}
