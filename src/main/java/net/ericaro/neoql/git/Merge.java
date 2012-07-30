package net.ericaro.neoql.git;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.ericaro.neoql.patches.PatchBuilder;


/** Merge algorithm. Analyzes two compact changes from a common ancestor.
 * Compact changes can only have 4 possible values ( o not present, D delete, I insert, Update ).
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
 * o    x		
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

hence the merge algorithm, will update  create a new git checkout from the common ancestor, and will apply
all the non conflicting changes. It will leave this "checkout" as is, with the list of remaining conflit to be solved.
Every conflict comes with a "top" level strategy to solve ( like prefer the local version)
once every conflict have been solved, the database can be tagged, and merged can be created.
 * 
 * 
 * 
 * @author eric
 *
 */

public class Merge {
	// TODO build conflict object, that are linked to this merge, that contains "resolution" (hard coded) and 
	// also the capability to solve the conflict "by hand" (ie, user work on the builder directly, and "simply" mark it as resolved".
	
	
	PatchBuilder local;
	PatchBuilder remote;
	PatchBuilder merged = new PatchBuilder();
	Map<Object, Object> localU ;
	Map<Object, Object> remoteU ;
	Set<Object> uu = new HashSet<Object>() ; // UU objects
	Set<Object> du = new HashSet<Object>() ; // UU objects
	Set<Object> ud = new HashSet<Object>() ; // UU objects
	
	
	public Merge(PatchBuilder local, PatchBuilder remote) {
		super();
		this.local = local;
		this.remote = remote;
		localU = local.getUpdated();
		remoteU = remote.getUpdated();
		merge();
	}

	
	public boolean hasConflicts() {
		return uu.size()>0 || du.size()>0 || ud.size()>0 ;
	}
	
	void merge() {
		//TODO merge table creation first
		mergeInserts();
		mergeUpdates();
		mergeDeletes();
	}
	public void mergeInserts() {
		// creates a PatchBuilder with as more merged as possible. Then returns a set of conflicts
		//parse in the standard order, I first, then U, then D
		for (Object i : local.getInserted() )
			merged.insert(i );
		for (Object i : remote.getInserted() )
			merged.insert(i );
		// I are always nice ;-)
	}

	private void mergeUpdates() {
		//merge locals
		for (Entry<Object, Object>  e: localU.entrySet() ) {
			Object src = e.getValue(); // the src, is present in the common ancestor (by definition)
			Object newValue = e.getKey();
			
			if (remoteU.containsValue(src))
				uu.add(src);
			else if (remote.getDeleted().contains(src))
				ud.add(src);
			else // yes ! no conflict
				merged.update(src, newValue );
		}
		//merge remotes 
		for (Entry<Object, Object>  e: remoteU.entrySet() ) {
			Object src = e.getValue(); // the src, is present in the common ancestor (by definition)
			Object newValue = e.getKey();
			
			if (localU.containsValue(src))
				assert uu.contains(src) : "assymetrical conflict";
			else if (local.getDeleted().contains(src))
				du.add(src);
			else // yes ! no conflict
				merged.update(src, newValue );
		}
	}
	public void mergeDeletes() {
		for (Object i : local.getDeleted() )
			if (!du.contains(i))
				merged.delete(i);
		for (Object i : remote.getDeleted() )
			if (!ud.contains(i))
			merged.delete(i );
		
	}
	
	
	
//	
//	static enum ConflictType {
//		Insert,// remote only
//		Delete,// remote only
//		Update, // remote only
//		DeleteUpdate, // local,remote 
//		UpdateDelete,
//		UpdateUpdate,
//		
//	}
//	
//	static class Conflict<T> {
//		T local, base, remote;
//		ConflictType	type;
//
//		public Conflict(ConflictType type, T local, T base, T remote) {
//			super();
//			this.type = type;
//			this.local = local;
//			this.base = base;
//			this.remote = remote;
//		}
//		
//		/*
//		for each case, I need to set a "resolution". Some conflict are trivial, and the resolution might
//		be trivial too ( U,I,D are trivial, DU,UD, UU are not and require human intervention).
//		bottom line, I need to 
//		1/ set a solution in the form of an instance.
//		2/ mark the conflict as solved. (a simple boolean is enough)
//		
//		I need to "offer" solutions like: 
//			accept all incoming (incoming always (DU,UD,UU) overwrite local)
//			ignore all incoming ( local always (DU,UU,UD) overwrite incoming)
//		
//		
//		problem: if I insert a I. its instance might have a foreign key to a one that is in a UU.
//		hence, whatever is the solution, I need to update I to point to whatever is the solution of this conflict.
//		The same apply for two instances linked in two separated UU.
//		
//		here are the cases:
//		I -> insert the instance + "take care" of the FK
//		D -> delete the instance + check that it has not foreign key (this will be check during the transaction
//		U -> update the instance + "take care" of FK (just like in I and like in any U or I involving a foreign key 
//		
//		DU -> either insert back the deletee, and update it, or delete it
//		UD -> either do nothing, or delete the entity
//		UU -> merge field by field, using the global strategy (priority to R or L), do not mark as "resolved"
//		
//		I need an idea to reuse the "usual" editors to merge. The ideal would be that the same
//		editor be used (or slightly adapted) to merge.
//		idea: provide a git merge and git conflict command. Git merge does not do anything ( appart from
//		filling the conflict set).
//		git conflict provide access to the conflict map letting the gui apply those changes.
//		a basic gui free merger could try to apply a "brute" force strategy. (kind of priority to the remote).
//		
//		A more advanced one could start "editor" part do display the conflict, and build the solution.
//		 
//		 
//		 FK resolution: for each delete (local or remote) I need to check if there are FK issues.
//		 if so: there are only two solutions: 
//		 drop the delete ( reinstanciate the entity) (easier as there are no further consequences
//		 drop the entity in every FK ( nullify, or set a default value, this require some capability though, and then delegation to the calling code).
//		 (dropping the entity in every FK including in the ones in the merge.
//		 
//		 => new type of conflict : DFK
//		
//		*/
//
//		
//		
//		@Override
//		public String toString() {
//			return type+" [local=" + local + ", base=" + base + ", remote=" + remote + "]";
//		}
//	}
//
//	/** turn two compact changes from a common ancestor into a set of conflict for the local point of view.
//	 * 
//	 * @param local
//	 * @param remote
//	 */
//	public static void merge(Map<Object, RowChange> local, Map<Object, RowChange> remote) {
//		Set<Conflict> conflicts = new HashSet<Conflict>();
//		for (Entry<Object, RowChange> e : remote.entrySet()) {
//			RowChange rr = e.getValue(); // remote change
//			Object base = e.getKey();
//			RowChange lr = local.get(base ); // local change
//			switch (rr.getType()) {
//			case I:
//				assert lr == null : "impossible case I, !null"; // it is impossible to have the same value in both path(it should be)
//				conflicts.add(new Conflict(ConflictType.Insert, null, null, rr.getEnd()));
//				break;
//			case D:
//				// need to check that the deletee will not cause any trouble foreign key violation
//				if(lr == null) 
//					conflicts.add(new Conflict(ConflictType.Delete,null, null, rr.getEnd()));
//				else {
//					assert lr.getType() != ChangeType.I : "impossible case I,D";
//					if (lr.getType() == ChangeType.U)
//						new Conflict(ConflictType.UpdateDelete, lr.getEnd(),base, rr.getEnd() );
//				}
//				break;
//			case U:
//				if (lr == null)
//					conflicts.add(new Conflict(ConflictType.Update, null, null, rr.getEnd()));
//				else if (lr.getType()== ChangeType.D)
//					new Conflict(ConflictType.DeleteUpdate, lr.getEnd(),base, rr.getEnd() );
//				else if (lr.getType()== ChangeType.U)
//					new Conflict(ConflictType.UpdateUpdate, lr.getEnd(),base, rr.getEnd() );
//			}
//		}
//	}
}
