package net.ericaro.neoql.system;

import java.util.Iterator;

import net.ericaro.neoql.Database;


public interface TableDef<T> {

	Table<T> asTable(Database database);

	Iterator<T> iterator(Database database);

}
