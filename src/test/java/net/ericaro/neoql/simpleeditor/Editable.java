package net.ericaro.neoql.simpleeditor;

/** Simple Editable POJO
 * 
 * @author eric
 *
 */
public class Editable implements HasName{

	private String name;
	private Directory parent;
	private boolean editing= false;
	public String getName() {
		return name;
	}
	public Directory getParent() {
		return parent;
	}
	public boolean isEditing() {
		return editing;
	}
	
	
	
}
