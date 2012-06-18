package net.ericaro.neoql.changeset;

public interface Change {

	void commit();
	void revert();
	
}
