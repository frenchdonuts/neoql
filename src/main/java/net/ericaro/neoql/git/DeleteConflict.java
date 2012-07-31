package net.ericaro.neoql.git;

/** Holds information about a delete conflict, either update/delete or delete/update.
 * 
 * @author eric
 *
 */
public class DeleteConflict<T> {
	Merge merge;
	T src;
	T updated;
	boolean deletedOnSource;
	DeleteConflict(Merge merge, T src, T updated, boolean deletedOnSource) {
		super();
		this.merge = merge;
		this.src = src;
		this.updated = updated;
		this.deletedOnSource = deletedOnSource;
	}
	
	/** calling this method totally ignore both changes, mark them as resolved outside. It is 
	 * your responsibility to merge the conflict
	 * 
	 */
	public void markAsResolved() {
		if (deletedOnSource)
			merge.du.remove(src);
		else
			merge.ud.remove(src);
	}
	
	/**resolve the conflict using the deletion anyway
	 * 
	 */
	public void resolveDeleting() {
		merge.merged.delete(src);
		markAsResolved();
	}
	/** resolve the conflict ignoring the deletion, and reinstanciating the update
	 * 
	 */
	public void resolveUpdating() {
		merge.merged.update(src, updated);
		markAsResolved();
	}
	
	/** resolve using whatever is the remote change
	 * 
	 */
	public void resolveRemote() {
		if(deletedOnSource)
			resolveUpdating();
		else
			resolveDeleting();
	}
	/** resolve using whatever is the local change
	 * 
	 */
	public void resolveLocal() {
		if(deletedOnSource)
			resolveDeleting();
		else
			resolveUpdating();
	}
}
