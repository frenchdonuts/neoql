package net.ericaro.neoql.changeset;

/** a change that return the commit and revert methods
 * 
 * @author eric
 *
 */
public class ReverseChange implements Change{

	
	protected Change change;
	
	public ReverseChange(Change src) {
		
	}

	/** return the original change (that is reversed here)
	 * 
	 * @return
	 */
	public Change getSource() {
		return change;
	}
	
	
	@Override
	public void commit() {
		change.revert();
	}

	@Override
	public void revert() {
		change.commit();
	}

	@Override
	public Change copy() {
		return new ReverseChange(change.copy());
	}
	public void accept(ChangeVisitor visitor) {visitor.changed(this);}
}
