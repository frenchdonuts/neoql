package net.ericaro.neoql.patches;


public class Update<T> implements Patch {
	protected T oldValue;
	protected T newValue;
	protected Class<T> key;
	
	public Update(Class<T> key, T oldValue, T newValue) {
		super();
		this.key = key;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	public Class<T> getType() {
		return key;
	}
	
	public T getOldValue() {
		return oldValue;
	}

	public T getNewValue() {
		return newValue;
	}

	public boolean contains(Object t) {
		return t==oldValue || t==newValue;
	}
	
	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

	public String toString() {
		return Patches.toString(this);
	}

}
