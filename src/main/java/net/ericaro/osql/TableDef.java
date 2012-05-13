package net.ericaro.osql;

public interface TableDef<T> {
	
	Table<T> asTable(Database database); 
	
}
