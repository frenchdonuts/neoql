package net.ericaro.neoql.changeset;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


/** A set of changes. They can be applied or reverted on the given database.
 * 
 * It keeps a weak reference to its parent, so that if you don't store every changeset, they will be
 * forget.
 * 
 * @author eric
 *
 */
public class ChangeSet implements Change{

	List<Change> operations = new ArrayList<Change>();
	Reference<ChangeSet> parent ;
	// TODO when I'll handle merge, need to have a list of merge parent
	
	
	public ChangeSet() {
		super();
		parent = new WeakReference<ChangeSet>(this);
	}
	


	public ChangeSet(ChangeSet parent) {
		super();
		this.parent = new WeakReference<ChangeSet>(parent);
	}


	/** return the parent's changeset if available. A change set only keep a weak reference to its parent.
	 * This means that, you can rely on this method if you keep references for all change set somewhere.
	 * 
	 * @return
	 */
	public ChangeSet getParent() {
		return parent.get();
	}



	public void addChange(Change... operation) {
		for (Change o: operation)
			if(o !=null)
				operations.add(o);
	}
	
	
	/** Apply stored operations
	 * 
	 */
	public void commit() {
		for(Change o: operations)
			o.commit() ;
	}
	/** revert stored operations
	 * 
	 */
	public void revert() {
		for(int i=operations.size();i>0;i--)
			operations.get(i-1).revert() ;
	}


	public boolean isEmpty() {
		return operations.size() == 0;
	}
	
	
	public ChangeSet clone(ChangeSet newParent) {
		ChangeSet that = copy();
		that.parent = new WeakReference<ChangeSet>(newParent);
		return that;
	}
	public ChangeSet copy() {
		ChangeSet that = new ChangeSet();
		for(Change c: operations)
			that.operations.add(c.copy());
		return that;
	}
}
