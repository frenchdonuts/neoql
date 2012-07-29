package net.ericaro.neoql.changeset;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class ChangeSet implements Change, Iterable<Change>{

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
	
	
	@Override
	public Iterator<Change> iterator() {
		return Collections.unmodifiableList(operations).iterator();
	}
	
	public List<Change> changes() {
		return Collections.unmodifiableList(operations);
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
			that.operations.add(0, c.reverse() );
		return that;
	}
	
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
	
	public String toString() {
		return Changes.toString(this);
	}
}
