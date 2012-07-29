package net.ericaro.neoql;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;
import net.ericaro.neoql.changeset.Changes;


public class DeleteChange<T> implements Change {
	protected Set<T> deleted = new HashSet<T>();
	
	Class<T> key;
	
	public DeleteChange(Class<T> key) {
		super();
		this.key = key;
	}
	


	public Class<T> getType() {
		return key;
	}

	
	@Override
	public Change copy() {
		DeleteChange<T> that = new DeleteChange<T>(key);
		for (T t : this.deleted)
			that.deleted.add(t);
		return that;
	}
	
	
	
	@Override
	public Change reverse() {
		InsertChange<T> i = new InsertChange<T>(key);
		i.inserted.addAll(deleted);
		return i;
	}



	public boolean contains(T row) {return deleted.contains(row);}
	public Iterable<T> deleted() {return Collections.unmodifiableCollection(deleted);}
	
	void delete(T row) {
		deleted.add(row);
	}
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
	
	public String toString() {
		return Changes.toString(this);
	}
}
