package net.ericaro.neoql.patches;


public class Insert<T> implements Patch {
	protected T inserted ;
	protected Class<T> key;
	
	public Insert(Class<T> key, T deleted) {
		super();
		this.key = key;
		this.inserted = deleted;
	}

	public Class<T> getType() {
		return key;
	}

	public T getInserted() {
		return inserted;
	}
	
	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

	public String toString() {
		return Patches.toString(this);
	}

}
