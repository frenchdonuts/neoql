package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Set;

public abstract class InsertChange<T> implements Change {
	Class<T> table;
	protected Set<T> inserted = new HashSet<T>();
	
	
	public void insert(T row) {
		inserted.add(row);
	}
	
}
