package net.ericaro.neoql;

import java.util.Arrays;


/**
 * Update from *
 * 
 * @author eric
 * 
 * @param <T>
 */
public class Update<T> implements Statement {

	private ClassTableDef<T> type;
	private ColumnValue<T, ?>[] columnValuePairs = new ColumnValue[0];
	private Predicate<? super T> where;

	public Update(ClassTableDef<T> type) {
		this.type = type;
	}

	Update(ClassTableDef<T> type, Predicate<? super T> where,
			ColumnValue<T, ?>... columnValuePairs) {
		super();
		this.type = type;
		this.columnValuePairs = columnValuePairs;
		this.where = where;
	}

	public <V> Update<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValue<T, V>((AbstractColumn<T, V>) col, value);
		return this;
	}

	public Update<T> where(Predicate<? super T> where) {
		this.where = where;
		return this;
	}

	public ClassTableDef<T> getTable() {
		return type;
	}

	public ColumnValue<T, ?>[] getColumnValuePairs() {
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
