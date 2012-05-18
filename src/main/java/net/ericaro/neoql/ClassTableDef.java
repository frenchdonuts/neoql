package net.ericaro.neoql;

import java.util.Iterator;


public class ClassTableDef<T> implements TableDef<T> {

	Class<T> table;

	 ClassTableDef(Class<T> table) {
		super();
		this.table = table;
	}

	public Class<T> getTable() {
		return table;
	}

	@Override
	public TableData<T> asTable(Database database) {
		return database.tableFor(table);
	}

	@Override
	public Iterator<T> iterator(final Database database) {
				return database.tableFor(table).iterator();
	}
	
	

}
