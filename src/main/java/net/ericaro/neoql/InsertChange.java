package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.changeset.Changes;




public class InsertChange<T> implements Change {
	Class<T> table;
	protected Set<T> inserted = new HashSet<T>();
	
Class<T> key;
	
	public InsertChange(Class<T> key) {
		super();
		this.key = key;
	}
	


	public Class<T> getType() {
		return key;
	}
	
	@Override
	public Change copy() {
		InsertChange<T> that = new InsertChange<T>(key);
		for (T t : this.inserted)
			that.inserted.add(t);
		return that;
	}
	
	@Override
	public Change reverse() {
		DeleteChange<T> d = new DeleteChange<T>(key);
		d.deleted.addAll(inserted);
		return d;
	}
	
	void insert(T row) {
		inserted.add(row);
	}
	
	public boolean contains(T row) {
		return inserted.contains(row);
	}
	
	boolean remove(T row) {
		return inserted.remove(row);
	}
	
	public Iterable<T> inserted(){ return Collections.unmodifiableCollection(inserted);}
	
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
	
	public String toString() {
		return Changes.toString(this);
	}



	public boolean isEmpty() {
		return inserted.isEmpty();
	}
}
