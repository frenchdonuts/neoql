package net.ericaro.neoql;

import java.util.Iterator;


public interface TableDef<T> {

	Table<T> asTable(Database database);

	Iterator<T> iterator(Database database);

}
