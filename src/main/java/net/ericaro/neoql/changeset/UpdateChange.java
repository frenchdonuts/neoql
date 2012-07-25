package net.ericaro.neoql.changeset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.ericaro.neoql.tables.Pair;

/** Change for table Data update
 * 
 * @author eric
 *
 * @param <T>
 */
public abstract class UpdateChange<T> implements Change {
	protected transient Map<T,T> updatedRows = new HashMap<T,T>(); 
	
	
	public boolean containsOld(T row) {
		return updatedRows.containsKey(row);
	}
	public boolean containsNew(T row) {
		return updatedRows.containsValue(row);
	}
	
	public Iterable<T> newValues(){
		return updatedRows.values();
	}
	
	public void remove(T row) {
		if (updatedRows.containsKey(row))
			updatedRows.remove(updatedRows.get(row));
		else 
			updatedRows.remove(row);
	}
	
	/** store this update in the transaction
	 * 
	 * @param oldValue
	 * @param newValue
	 */
	public void update(T oldValue, T newValue) {
		if (oldValue != newValue) // updates made to existing new Value 
			updatedRows.put(oldValue, newValue);
	}
	
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
}
