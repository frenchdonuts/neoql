package net.ericaro.neoql;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;

/** Change for table Data update
 * 
 * @author eric
 *
 * @param <T>
 */
public class UpdateChange<T> implements Change {
	protected transient Map<T,T> updatedRows = new HashMap<T,T>(); 
	Class<T> key;
	
	public UpdateChange(Class<T> key) {
		super();
		this.key = key;
	}
	


	public Class<T> getType() {
		return key;
	}



	@Override
	public Change copy() {
		UpdateChange<T> that = new UpdateChange<T>(key);
		for (Entry<T, T> e : this.updatedRows.entrySet())
			that.updatedRows.put(e.getKey(), e.getValue());
		return that;
	}
	@Override
	public Change reverse() {
		UpdateChange<T> that = new UpdateChange<T>(key);
		for (Entry<T, T> e : this.updatedRows.entrySet())
			that.updatedRows.put(e.getValue(), e.getKey());
		return that;
	}

	
	public boolean containsOld(T row) {
		return updatedRows.containsKey(row);
	}
	public boolean containsNew(T row) {
		return updatedRows.containsValue(row);
	}
	
	public Iterable<Entry<T, T>> updates() {
		return updatedRows.entrySet();
	}
	
	public Iterable<T> newValues(){
		return updatedRows.values();
	}
	
	
	void remove(T row) {
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
	void update(T oldValue, T newValue) {
		if (oldValue != newValue) // updates made to existing new Value 
			updatedRows.put(oldValue, newValue);
	}
	
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
}
