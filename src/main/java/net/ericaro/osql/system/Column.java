package net.ericaro.osql.system;

import java.lang.reflect.Field;

public class Column<T,V> {

	Field field;
	Class<V> foreignTable;
	String fname;
	
	
	
	
	public Column(String fname) {
		super();
		this.fname = fname;
	}

	public Column(String fname, Class<V> foreignTable) {
		this(fname);
		this.foreignTable = foreignTable;
	}

	void copy(Object src, Object target ) {
		set(target, get(src) );
	}
	
	protected void set(Object src, V value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}
	protected V get(Object src) {
		try {
			return (V) field.get(src);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}

	public Class<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return foreignTable != null;
	}
	
	
}
