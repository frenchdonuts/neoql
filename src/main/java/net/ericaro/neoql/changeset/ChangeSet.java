package net.ericaro.neoql.changeset;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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
	// TODO when I'll handle merge, need to have a list of merge parent
	
	
	public ChangeSet(Change... operation) {
		this(Arrays.asList(operation));
	}
	public ChangeSet(Iterable<Change> operation) {
		super();
		addChange(operation);
	}

	private void addChange(Iterable<Change> operation) { // shouldn't be public
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

	public ChangeSet copy() {
		ChangeSet that = new ChangeSet();
		for(Change c: operations)
			that.operations.add(c.copy());
		return that;
	}
	/** returns a reverse copy of this changeset. 
	 * 
	 * @return
	 */
	public ChangeSet reverse() {
		ChangeSet that = new ChangeSet();
		for(Change c: operations)
			that.operations.add(0, new ReverseChange(c) );
		return that;
	}
	
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
	
}
