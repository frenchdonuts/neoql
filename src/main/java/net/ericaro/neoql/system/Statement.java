package net.ericaro.neoql.system;

import net.ericaro.neoql.Database;


public interface Statement {

	void executeOn(Database database);

}
