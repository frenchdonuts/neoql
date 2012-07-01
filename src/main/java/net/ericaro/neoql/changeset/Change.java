package net.ericaro.neoql.changeset;

/** Every changes implement this interface
 * 
 * @author eric
 *
 */
public interface Change {

	void commit();
	void revert();
	Change copy();
	
}
