package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableDef;

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

	public Select(Class<T> table, Predicate<? super T> where) {
		this(new ClassTableDef<T>(table), where);
	}

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

}
