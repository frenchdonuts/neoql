package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.List;

/** A transaction is a list of operations
 * 
 * @author eric
 *
 */
public class ChangeSet {

	List<Change> operations = new ArrayList<Change>();
	ChangeSet parent ;
	// TODO when I'll handle merge, need to have a list of merge parent
	
	
	public ChangeSet() {
		super();
		parent = this;
	}
	


	public ChangeSet(ChangeSet parent) {
		super();
		this.parent = parent;
	}


	public ChangeSet getParent() {
		return parent;
	}



	public void addChange(Change... operation) {
		for (Change o: operation)
			operations.add(o);
	}
	
	
	/** Apply stored operations
	 * 
	 */
	void commit() {
		for(Change o: operations)
			o.commit() ;
	}
	/** revert stored operations
	 * 
	 */
	void revert() {
		for(int i=operations.size();i>0;i--)
			operations.get(i-1).revert() ;
	}


	public boolean isEmpty() {
		return operations.size() == 0;
	}
	
}
