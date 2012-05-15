package net.ericaro.neoql;

 class ClassTableDef<T> implements TableDef<T> {

	Class<T> table;

	 ClassTableDef(Class<T> table) {
		super();
		this.table = table;
	}

	 Class<T> getTable() {
		return table;
	}

	@Override
	  public TableData<T> asTable(Database database) {
		return database.tableFor(table);
	}

	
	
	
	
	
}
