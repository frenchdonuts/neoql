package net.ericaro.neoql.changeset;

import java.util.HashSet;
import java.util.Set;


public abstract class DeleteChange<T> implements Change {
	protected Set<T> deleted = new HashSet<T>();
	
	public boolean contains(T row) {return deleted.contains(row);}
	
	public void delete(T row) {
		deleted.add(row);
	}

	
}
