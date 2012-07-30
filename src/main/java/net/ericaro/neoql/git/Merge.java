package net.ericaro.neoql.git;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.ericaro.neoql.git.CompactChanges.RowChange;
import net.ericaro.neoql.git.CompactChanges.RowChange.ChangeType;

public class Merge {
	
	static enum ConflictType {
		Insert,// remote only
		Delete,// remote only
		Update, // remote only
		DeleteUpdate, // local,remote 
		UpdateDelete,
		UpdateUpdate,
		
	}
	
	static class Conflict<T> {
		T local, base, remote;
		ConflictType	type;

		public Conflict(ConflictType type, T local, T base, T remote) {
			super();
			this.type = type;
			this.local = local;
			this.base = base;
			this.remote = remote;
		}
		
		/*
		for each case, I need to set a "resolution". Some conflict are trivial, and the resolution might
		be trivial too ( U,I,D are trivial, DU,UD, UU are not and require human intervention).
		bottom line, I need to 
		1/ set a solution in the form of an instance.
		2/ mark the conflict as solved. (a simple boolean is enough)
		
		I need to "offer" solutions like: 
			accept all incoming (incoming always (DU,UD,UU) overwrite local)
			ignore all incoming ( local always (DU,UU,UD) overwrite incoming)
		
		
		problem: if I insert a I. its instance might have a foreign key to a one that is in a UU.
		hence, whatever is the solution, I need to update I to point to whatever is the solution of this conflict.
		The same apply for two instances linked in two separated UU.
		
		here are the cases:
		I -> insert the instance + "take care" of the FK
		D -> delete the instance + check that it has not foreign key (this will be check during the transaction
		U -> update the instance + "take care" of FK (just like in I and like in any U or I involving a foreign key 
		
		DU -> either insert back the deletee, and update it, or delete it
		UD -> either do nothing, or delete the entity
		UU -> merge field by field, using the global strategy (priority to R or L), do not mark as "resolved"
		
		I need an idea to reuse the "usual" editors to merge. The ideal would be that the same
		editor be used (or slightly adapted) to merge.
		idea: provide a git merge and git conflict command. Git merge does not do anything ( appart from
		filling the conflict set).
		git conflict provide access to the conflict map letting the gui apply those changes.
		a basic gui free merger could try to apply a "brute" force strategy. (kind of priority to the remote).
		
		A more advanced one could start "editor" part do display the conflict, and build the solution.
		 
		
		*/

		@Override
		public String toString() {
			return type+" [local=" + local + ", base=" + base + ", remote=" + remote + "]";
		}
	}

	/** turn two compact changes from a common ancestor into a set of conflict for the local point of view.
	 * 
	 * @param local
	 * @param remote
	 */
	public static void merge(Map<Object, RowChange> local, Map<Object, RowChange> remote) {
		Set<Conflict> conflicts = new HashSet<Conflict>();
		for (Entry<Object, RowChange> e : remote.entrySet()) {
			RowChange rr = e.getValue(); // remote change
			Object base = e.getKey();
			RowChange lr = local.get(base ); // local change
			switch (rr.getType()) {
			case I:
				assert lr == null : "impossible case I, !null"; // it is impossible to have the same value in both path(it should be)
				conflicts.add(new Conflict(ConflictType.Insert, null, null, rr.getEnd()));
				break;
			case D:
				// need to check that the deletee will not cause any trouble foreign key violation
				if(lr == null) 
					conflicts.add(new Conflict(ConflictType.Delete,null, null, rr.getEnd()));
				else {
					assert lr.getType() != ChangeType.I : "impossible case I,D";
					if (lr.getType() == ChangeType.U)
						new Conflict(ConflictType.UpdateDelete, lr.getEnd(),base, rr.getEnd() );
				}
				break;
			case U:
				if (lr == null)
					conflicts.add(new Conflict(ConflictType.Update, null, null, rr.getEnd()));
				else if (lr.getType()== ChangeType.D)
					new Conflict(ConflictType.DeleteUpdate, lr.getEnd(),base, rr.getEnd() );
				else if (lr.getType()== ChangeType.U)
					new Conflict(ConflictType.UpdateUpdate, lr.getEnd(),base, rr.getEnd() );
			}
		}
	}
}
