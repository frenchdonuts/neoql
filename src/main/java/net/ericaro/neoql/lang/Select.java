package net.ericaro.neoql.lang;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.SelectTable;
import net.ericaro.neoql.SelectTable.SelectIterator;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.Table;
import net.ericaro.neoql.system.TableDef;


/**
 * SELECT * FROM table WHERE
 * 
 * @author eric
 * 
 * @param <T>
 */
public class Select<T> implements TableDef<T> {

	TableDef<T> table;
	Predicate<? super T> where;


	public Select(TableDef<T> table, Predicate<? super T> where) {
		super();
		this.table = table;
		this.where = where;
	}

	public TableDef<T> getTable() {
		return table;
	}

	public Predicate<? super T> getWhere() {
		return where;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(this);
	}

	@Override
	public Iterator<T> iterator(final Database database) {
				return new SelectTable.SelectIterator<T>(database.select(table).iterator(), where);
	}

	
	
}
