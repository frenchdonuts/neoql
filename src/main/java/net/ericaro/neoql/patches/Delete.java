package net.ericaro.neoql.patches;


public class Delete<T> implements Patch {
	protected T deleted ;
	protected Class<T> key;
	
	public Delete(Class<T> key, T deleted) {
		super();
		this.key = key;
		this.deleted = deleted;
	}

	public Class<T> getType() {
		return key;
	}

	public T getDeleted() {
		return deleted;
	}
	
	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

	public String toString() {
		return Patches.toString(this);
	}

}
