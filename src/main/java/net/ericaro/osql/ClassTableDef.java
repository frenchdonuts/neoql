package net.ericaro.osql;

public class ClassTableDef<T> implements TableDef<T> {

	Class<T> table;

	public ClassTableDef(Class<T> table) {
		super();
		this.table = table;
	}

	public Class<T> getTable() {
		return table;
	}

	@Override
	public  TableData<T> asTable(Database database) {
		return database.tableFor(table);
	}

	
	
	
	
	
}
