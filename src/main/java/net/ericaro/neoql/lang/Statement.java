package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;

public interface Statement {

	void executeOn(Database database);

}
