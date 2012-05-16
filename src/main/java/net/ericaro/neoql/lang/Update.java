package net.ericaro.neoql.lang;

import java.util.Arrays;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;

/**
 * Update from *
 * 
 * @author eric
 * 
 * @param <T>
 */
public class Update<T> implements Statement {

	private Class<T> type;
	private ColumnValuePair<T, ?>[] columnValuePairs = new ColumnValuePair[0];
	private Predicate<? super T> where;

	public Update(Class<T> type) {
		this.type = type;
	}

	Update(Class<T> type, Predicate<? super T> where,
			ColumnValuePair<T, ?>... columnValuePairs) {
		super();
		this.type = type;
		this.columnValuePairs = columnValuePairs;
		this.where = where;
	}

	public <V> Update<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValuePair<T, V>(col, value);
		return this;
	}

	public Update<T> where(Predicate<? super T> where) {
		this.where = where;
		return this;
	}

	public Class<T> getTable() {
		return type;
	}

	public ColumnValuePair<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	public Predicate<? super T> getWhere() {
		return where;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
