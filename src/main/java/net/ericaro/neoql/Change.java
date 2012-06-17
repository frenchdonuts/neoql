package net.ericaro.neoql;

public interface Change {

	public void commit();
	void revert();
	
}
