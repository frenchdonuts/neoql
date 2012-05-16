package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableDef;

/**
 * SELECT * FROM table WHERE GROUP BY column
 * 
 * @author eric
 * 
 * @param <T>
 */
public class GroupBySelect<S, T> implements TableDef<T> {

	Column<S, T> groupBy;
	private TableDef<S> table;
	private Predicate<? super S> where;

	GroupBySelect(Class<S> table, Predicate<? super S> where,
			Column<S, T> groupBy) {
		this(new ClassTableDef<S>(table), where, groupBy);
	}

	GroupBySelect(TableDef<S> table, Predicate<? super S> where,
			Column<S, T> groupBy) {
		this.table = table;
		this.where = where;
		this.groupBy = groupBy;
	}

	public Column<S, T> getGroupBy() {
		return groupBy;
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
