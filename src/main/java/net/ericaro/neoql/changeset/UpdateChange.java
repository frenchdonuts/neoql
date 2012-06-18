package net.ericaro.neoql.changeset;

import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.Pair;

/** Change for table Data update
 * 
 * @author eric
 *
 * @param <T>
 */
public abstract class UpdateChange<T> implements Change {
	protected Set<Pair<T, T> > updated = new HashSet<Pair<T,T>>(); // pair old/new
	
	
	public void update(T oldValue, T newValue) {
		updated.add(new Pair<T,T>(oldValue, newValue));
	}
	
	
}
