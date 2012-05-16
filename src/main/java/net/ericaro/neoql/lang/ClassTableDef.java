package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.TableDef;

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
