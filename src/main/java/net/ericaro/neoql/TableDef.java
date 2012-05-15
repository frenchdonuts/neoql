package net.ericaro.neoql;

public interface TableDef<T> {
	
	Table<T> asTable(Database database); 
	
}
