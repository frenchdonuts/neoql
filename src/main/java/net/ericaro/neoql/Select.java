package net.ericaro.neoql;

import java.util.Iterator;


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
