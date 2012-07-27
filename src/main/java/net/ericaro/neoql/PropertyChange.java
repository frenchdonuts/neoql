package net.ericaro.neoql;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.changeset.ChangeVisitor;



public class PropertyChange<T> implements Change {
	protected T oldValue, newValue;
	
	Object key;
	
	public PropertyChange(Object key) {
		super();
		this.key = key;
	}
	


	public Object getKey() {
		return key;
	}
	
	
	// package to let only the content source the right to change a change
	void set(T oldValue, T newValue) {
		this.oldValue = oldValue;
		this.newValue = newValue ;
	}
	
	@Override
	public Change copy() {
		PropertyChange<T> that = new PropertyChange<T>(key);
		that.newValue = this.newValue;
		that.oldValue = this.oldValue;
		return that;
	}
	@Override
	public Change reverse() {
		PropertyChange<T> that = new PropertyChange<T>(key);
		that.newValue = this.oldValue;
		that.oldValue = this.newValue;
		return that;
	}
	
	public T getOldValue() {
		return oldValue;
	}
	public T getNewValue() {
		return newValue;
	}

	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
	
}
