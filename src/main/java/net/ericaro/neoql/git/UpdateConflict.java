package net.ericaro.neoql.git;

/** Holds information about a Update Update conflict
 * 
 * @author eric
 *
 */
public class UpdateConflict<T>  implements Conflict<T>{
	Merge merge;
	T src;
	T localUpdated;
	T remoteUpdated;
	
	
	public UpdateConflict(Merge merge, T src, T localUpdated, T remoteUpdated) {
		super();
		this.merge= merge;
		this.src = src;
		this.localUpdated = localUpdated;
		this.remoteUpdated = remoteUpdated;
	}
	
	/** calling this method totally ignore both changes, mark them as resolved outside. It is 
	 * your responsibility to mergeBuilder the conflict
	 * 
	 */
	public void markAsResolved() {
		merge.markResolved(this) ;
	}
	
	/** resolve using whatever is the remote change
	 * 
	 */
	public void resolveRemote() {
		merge.patchBuilder.update(src, remoteUpdated);
		markAsResolved();
	}
	/** resolve using whatever is the local change
	 * 
	 */
	public void resolveLocal() {
		merge.patchBuilder.update(src, localUpdated);
		markAsResolved();
	}

	@Override
	public String toString() {
		return "Update Conflict:\nbase  : " + src + "\nlocal : " + localUpdated + "remote: " + remoteUpdated ;
	}
	
}
