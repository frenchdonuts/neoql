package net.ericaro.neoql;

import java.lang.reflect.Field;

import net.ericaro.neoql.TableData.ForeignKeyColumnListener;

public class Column<T, V> implements Mapper<T,V>{

	Class<V> foreignTable;
	
	// that's part of the 
	Field field;
	String fname;

	
	public Column(String fname) {
		super();
		this.fname = fname;
	}

	public Column(String fname, Class<V> foreignTable) {
		this(fname);
		this.foreignTable = foreignTable;
	}

	public void copy(T src, T target ) {
		set(target, get(src) );
	}
	
	void set(T src, V value) {
		try {
			field.set(src, value);
		} catch (Exception e) {
			throw new RuntimeException("wrong field",e);
		}
	}
	
	
	@Override
	public V map(T source) {
		return get(source);
	}

	public V get(Object src) {
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
	
	public void init(Class<T> tableClass) throws NoSuchFieldException, SecurityException {
		if (field == null) {// not init
			field = tableClass.getDeclaredField(fname);
			field.setAccessible(true);
		}
	}
	
	
}
