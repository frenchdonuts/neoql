package net.ericaro.neoql;

public interface Content {

	
	void accept(ContentVisitor visitor);
}
