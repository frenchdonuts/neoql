package net.ericaro.neoql;

import java.util.Iterator;

import net.ericaro.neoql.GroupByTable.GroupByIterator;

/**
 * SELECT * FROM table WHERE GROUP BY column
 * 
 * @author eric
 * 
 * @param <T>
 */
public class GroupBySelect<S, T> implements TableDef<T> {

	Column<S, T>					groupBy;
	private TableDef<S>				table;

	GroupBySelect(Class<S> table,  Column<S, T> groupBy) {
		this(new ClassTableDef<S>(table), groupBy);
	}

	GroupBySelect(TableDef<S> table, Column<S, T> groupBy) {
		this.table = table;
		this.groupBy = groupBy;
	}

	public Column<S, T> getGroupBy() {
		return groupBy;
	}

	public TableDef<S> getTable() {
		return table;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(this);
	}

	@Override
	public Iterator<T> iterator(Database database) {
		return new GroupByIterator<S, T>(database.iterator(table), groupBy);
	}

}