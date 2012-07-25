package net.ericaro.neoql.simpleeditor;

/** Pojo describing a Directory of editables (or directories)
 * 
 * @author eric
 *
 */
public class Directory implements HasName{

	private String name;
	private Directory parent;
	public String getName() {
		return name;
	}
	public Directory getParent() {
		return parent;
	}
	
	
	
	
}
