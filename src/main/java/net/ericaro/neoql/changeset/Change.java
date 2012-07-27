package net.ericaro.neoql.changeset;

/** Every changes implement this interface
 * 
 * @author eric
 *
 */
public interface Change {

	Change copy();
	Change reverse();
	
	void accept(ChangeVisitor visitor);
	
}
