package net.ericaro.neoql;

import java.util.HashSet;
import java.util.Set;


public abstract class UpdateChange<T> implements Change {
	protected Set<Pair<T, T> > updated = new HashSet<Pair<T,T>>(); // pair old/new
	
	
	public void update(T oldValue, T newValue) {
		updated.add(new Pair<T,T>(oldValue, newValue));
	}
	
	
}
