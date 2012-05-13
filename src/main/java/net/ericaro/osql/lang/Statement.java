package net.ericaro.osql.lang;

import net.ericaro.osql.system.Database;

public interface Statement {

	void executeOn(Database database);

}
