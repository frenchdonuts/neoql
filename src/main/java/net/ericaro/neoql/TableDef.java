package net.ericaro.neoql;



public interface TableDef<T> {

	// visitor pattern for table creation
	Table<T> asTable(Database database);
}
