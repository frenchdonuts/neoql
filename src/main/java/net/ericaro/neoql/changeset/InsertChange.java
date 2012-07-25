package net.ericaro.neoql.changeset;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public abstract class InsertChange<T> implements Change {
	Class<T> table;
	protected Set<T> inserted = new HashSet<T>();
	
	
	public void insert(T row) {
		inserted.add(row);
	}
	
	public boolean contains(T row) {
		return inserted.contains(row);
	}
	
	public boolean remove(T row) {
		return inserted.remove(row);
	}
	
	public Iterable<T> values(){ return Collections.unmodifiableCollection(inserted);}
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
}
