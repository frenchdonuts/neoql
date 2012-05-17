package net.ericaro.neoql.lang;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableData;
import net.ericaro.neoql.TableDef;

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
	public TableData<T> asTable(Database database) {
		return database.tableFor(table);
	}

	@Override
	public Iterator<T> iterator(final Database database) {
				return database.tableFor(table).iterator();
	}
	
	

}
