package net.ericaro.neoql.git;

import java.util.HashSet;

import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.PatchBuilder;

/** contains the result of a merge Builder operation. Can be apply to the orginal repository
 * 
 * @author eric
 *
 */
public class Merge {

	HashSet<DeleteConflict>	deleteConflicts = new HashSet<DeleteConflict>();
	HashSet<UpdateConflict>	updateConflicts = new HashSet<UpdateConflict>();
	PatchBuilder patchBuilder;
	Commit	forward;
	Patch	remote; // the remote patch
	Patch	local; // the local patch, merge is about finding a middle state using local -> middle patch, and remote to middle pathc
	Commit	localHead;
	Commit	remoteHead;
	
	// special constructor for a fast forward merge
	Merge(Commit fastforward) {
		super();
		this.forward = fastforward;
	}
	
	Merge(PatchBuilder patchBuilder, Commit localHead, Commit remoteHead) {
		super();
		this.patchBuilder = patchBuilder;
		this.localHead = localHead;
		this.remoteHead = remoteHead;
	}
	void markResolved(DeleteConflict src) {
		deleteConflicts.remove(src);
	}
	void markResolved(UpdateConflict src) {
		updateConflicts.remove(src);
	}
	
	public boolean isFasForward() {
		return forward !=null;
	}
	
	public boolean hasConflicts() {
		return deleteConflicts.size()>0 || updateConflicts.size() >0 ;
	}
	
	
	public Iterable<DeleteConflict> deleteConflicts(){
		return deleteConflicts;
	}
	
	public Iterable<UpdateConflict> updateConflicts(){
		return updateConflicts;
	}
	
	public Iterable<Conflict> allConflicts(){
		HashSet<Conflict> all = new HashSet<Conflict>(deleteConflicts);
		all.addAll(updateConflicts);
		return all;
	}

}
