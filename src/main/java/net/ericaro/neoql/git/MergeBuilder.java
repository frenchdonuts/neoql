package net.ericaro.neoql.git;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.ericaro.neoql.patches.PatchBuilder;


/** MergeBuilder algorithm. Analyzes two compact changes from a common ancestor.
 * Compact changes can only have 4 possible values ( o not present, D delete, I insert, U Update ).
 * Deletion has a constraints in the database the the entity used should not be used. Hence for each deletion 
 * we must check that the instance is not in use in the other part. Hence we need to add another possible status for an instance: S as uSed.
 * 
 * therefore here we are: 25 (5x5 possibilities)
 * 		o	 I		S		U		D
 * o    
 * I
 * S
 * U
 * D
 * 
 * some situation are impossible (8)
 * 
 * 		o		I		S		U		D
 * o	x		
 * I			x		x		x		x
 * S			x
 * U			x
 * D			x
 * 
 * in the remaining some are not conflicts (11)
 * 
 * 		o	 I		S		U		D
 * o    	 x		x		x		x
 * I	x			
 * S	x	 		x		x
 * U	x			x
 * D	x


this leave us with 25 -8-11 = 6 conflictual situation, called after their coordinate.

 * 		S		U		D
 * S					SD
 * U			UU		UD
 * D	DS		DU		DD

hence the mergeBuilder algorithm, will update  create a new git checkout from the common ancestor, and will apply
all the non conflicting changes. It will leave this "checkout" as is, with the list of remaining conflit to be solved.
Every conflict comes with "top" level strategies to solve ( like prefer the local version )
once every conflict have been solved, the database can be tagged, and a merge can be created.
 * 
 * @author eric
 *
 */

public class MergeBuilder {
	// TODO build conflict object, that are linked to this mergeBuilder, that contains "resolution" (hard coded) and 
	// also the capability to solve the conflict "by hand" (ie, user work on the builder directly, and "simply" mark it as resolved".
	
	
	
	PatchBuilder local;
	PatchBuilder remote;
	PatchBuilder merged = new PatchBuilder();
	Map<Object, Object> localU = new HashMap<Object,Object>() ; // UU objects
	Map<Object, Object> remoteU = new HashMap<Object,Object>() ; // UU objects
	Set<Object> uu = new HashSet<Object>() ; // UU objects
	Map<Object, Object> du = new HashMap<Object,Object>() ; // UU objects
	Map<Object, Object> ud = new HashMap<Object,Object>() ; // UU objects
	private HashSet<DeleteConflict>	deleteConflicts;
	private HashSet<UpdateConflict>	updateConflicts;
	Merge merge ;
	private Commit	base;
	
	public MergeBuilder(Commit base, Commit localHead, PatchBuilder local, Commit remoteHead, PatchBuilder remote) {
		super();
		this.base = base;
		this.local = local;
		this.remote = remote;
		Map<Object, Object> lU = local.getUpdated();
		Map<Object, Object> rU = remote.getUpdated();
		for(Entry<Object, Object> e: lU.entrySet())
			localU.put(e.getValue(), e.getKey());
		for(Entry<Object, Object> e: rU.entrySet())
			remoteU.put(e.getValue(), e.getKey());
			
		merge = new Merge(merged, base, localHead, remoteHead);
		merge();
		buildConflicts();
		
	}
	
	public Merge build() {
		merge.local = local.build();
		merge.remote = remote.build();
		return merge;
	}
	

	void buildConflicts() {
		merge.deleteConflicts = new HashSet<DeleteConflict>();
		for(Entry<Object,Object> e : du.entrySet()) 
			merge.deleteConflicts.add(new DeleteConflict(merge, e.getKey(), e.getValue(), true));
		
		merge.updateConflicts = new HashSet<UpdateConflict>();
		for(Object src : uu) 
			merge.updateConflicts.add(new UpdateConflict(merge, src,   localU.get(src) , remoteU.get(src)));
		
	}

	/** returns true if it has 
	 * 
	 * @return
	 */
	boolean hasConflicts() {
		return uu.size()>0 || du.size()>0 || ud.size()>0 ;
	}
	
	void merge() {
		//TODO mergeBuilder table creation first
		mergeInserts();
		mergeUpdates();
		mergeDeletes();
	}
	void mergeInserts() {
		// creates a PatchBuilder with as more merged as possible. Then returns a set of conflicts
		//parse in the standard order, I first, then U, then D
		for (Object i : local.getInserted() )
			merged.insert(i );
		for (Object i : remote.getInserted() )
			merged.insert(i );
		// I are always nice ;-)
	}

	void mergeUpdates() {
		//mergeBuilder locals
		for (Entry<Object, Object>  e: localU.entrySet() ) {
			Object src = e.getKey(); // the src, is present in the common ancestor (by definition)
			Object lValue = e.getValue();
			Object rValue = remoteU.get(src); // possibly null , means that the same src has been updated in both parts
			
			if (rValue !=null) // means that there is an uu conflict
				uu.add(src);
			else if (remote.getDeleted().contains(src)) // was remotely deleted
				ud.put(src, lValue);
			else // yes ! no conflict
				merged.update(src, lValue );
		}
		
		//mergeBuilder remotes 
		for (Entry<Object, Object>  e: remoteU.entrySet() ) {
			Object src = e.getKey(); // the src, is present in the common ancestor (by definition)
			Object newValue = e.getValue();
			
			Object newLocalValue = localU.get(src);
			
//			if (localU.containsKey(src))
//				assert newLocalValue==null : "not symetrical conflict";
//			else 
			if (local.getDeleted().contains(src))
				du.put(src,newValue);
			else // yes ! no conflict
				merged.update(src, newValue );
		}
	}
	void mergeDeletes() {
		for (Object i : local.getDeleted() )
			if (!du.containsKey(i))
				merged.delete(i);
		for (Object i : remote.getDeleted() )
			if (!ud.containsKey(i))
			merged.delete(i );
		
	}
	
	
	
}
