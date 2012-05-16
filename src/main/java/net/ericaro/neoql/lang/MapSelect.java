package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.Mapper;
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
public class MapSelect<S, T> implements TableDef<T> {

	Mapper<S, T> mapper;
	private TableDef<S> table;
	private Predicate<? super S> where;

	MapSelect(Mapper<S, T> mapper, Class<S> table, Predicate<? super S> where) {
		this(mapper, new ClassTableDef<S>(table), where);
	}

	MapSelect(Mapper<S, T> mapper, TableDef<S> table, Predicate<? super S> where) {
		this.table = table;
		this.where = where;
		this.mapper = mapper;
	}

	public Mapper<S, T> getMapper() {
		return mapper;
	}

	public TableDef<S> getTable() {
		return table;
	}

	public Predicate<? super S> getWhere() {
		return where;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(this);
	}

}
