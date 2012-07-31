package net.ericaro.neoql.git;

/** Holds information about a delete conflict, either update/delete or delete/update.
 * 
 * @author eric
 *
 */
public class DeleteConflict<T> implements Conflict<T> {
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
	 * your responsibility to mergeBuilder the conflict
	 * 
	 */
	public void markAsResolved() {
			merge.markResolved(this);
	}
	
	/**resolve the conflict using the deletion anyway
	 * 
	 */
	public void resolveDeleting() {
		merge.patchBuilder.delete(src);
		markAsResolved();
	}
	/** resolve the conflict ignoring the deletion, and reinstanciating the update
	 * 
	 */
	public void resolveUpdating() {
		merge.patchBuilder.update(src, updated);
		markAsResolved();
	}
	
	/* (non-Javadoc)
	 * @see net.ericaro.neoql.git.Conflict#resolveRemote()
	 */
	@Override
	public void resolveRemote() {
		if(deletedOnSource)
			resolveUpdating();
		else
			resolveDeleting();
	}
	/* (non-Javadoc)
	 * @see net.ericaro.neoql.git.Conflict#resolveLocal()
	 */
	@Override
	public void resolveLocal() {
		if(deletedOnSource)
			resolveDeleting();
		else
			resolveUpdating();
	}
}
