package net.ericaro.neoql.lang;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.GroupByTable.GroupByIterator;
import net.ericaro.neoql.OrderByTable.OrderByIterator;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.TableDef;

/**
 * SELECT * FROM table WHERE ORDER BY column
 * 
 * @author eric
 * 
 * @param <T>
 */
public class OrderBySelect<T,V extends Comparable<? super V>> implements TableDef<T> {

	Column<T,V>					orderBy;
	private TableDef<T>				table;
	private boolean ascendent = true;

	OrderBySelect(Class<T> table,  Column<T,V> orderBy, boolean ascendent) {
		this(new ClassTableDef<T>(table), orderBy, ascendent);
	}

	OrderBySelect(TableDef<T> table, Column<T,V> orderBy,boolean ascendent) {
		this.table = table;
		this.orderBy = orderBy;
		this.ascendent = ascendent;
	}

	public Column<T,V> getOrderBy() {
		return orderBy;
	}

	public boolean isAscendent() {
		return ascendent;
	}

	public TableDef<T> getTable() {
		return table;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(this);
	}

	@Override
	public Iterator<T> iterator(Database database) {
		return new OrderByIterator<T,V>( database.iterator(table), orderBy, ascendent);
	}

}
