package net.ericaro.neoql.lang;

import java.util.Arrays;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Column;
import net.ericaro.neoql.system.Statement;


/**
 * INSERT INTO table set column = value, column= value
 * 
 * @author eric
 * 
 */
public class InsertInto<T> implements Statement {

	private ClassTableDef<T> table;
	private ColumnValue<T, ?>[] columnValuePairs = new ColumnValue[0];

	InsertInto(ClassTableDef<T> type) {
		super();
		this.table = type;
	}

	public <V> InsertInto<T> set(Column<T, V> col, V value) {
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l + 1);
		columnValuePairs[l] = new ColumnValue<T, V>((ColumnDef<T, V>) col, value);
		return this;
	}

	public ClassTableDef<T> getTable() {
		return table;
	}

	ColumnValue<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}

	T build() {
		T row = table.newInstance();
		for (ColumnValue s : columnValuePairs)
			((ColumnDef)s.getColumn()).set(row, s.getValue());
		return row;
	

}
	
	@Override
	public void executeOn(Database database) {
		database.execute(this, build());
	}
}
