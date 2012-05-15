package net.ericaro.osql;

import java.util.Arrays;


/** Update from 
 * *
 * @author eric
 *
 * @param <T>
 */
 public class Update<T> implements Statement {

	private Class<T> type;
	private ColumnValuePair<T,?>[] columnValuePairs = new ColumnValuePair[0];
	private Predicate<? super T> where;
	
	Update(Class<T> type) {
		this.type = type;
	}
	
	 Update(Class<T> type, Predicate<? super T> where, ColumnValuePair<T, ?>... columnValuePairs
			) {
		super();
		this.type = type;
		this.columnValuePairs = columnValuePairs;
		this.where = where;
	}
	

	 public <V> Update<T> set(Column<T, V> col, V value){
		int l = columnValuePairs.length;
		columnValuePairs = Arrays.copyOf(columnValuePairs, l+1);
		columnValuePairs[l] = new ColumnValuePair<T, V>(col, value);
		return this;
	}
	
	 public Update<T> where(Predicate<? super T> where){
		this.where = where;
		return this;
	}
	
	 Class<T> getType() {
		return type;
	}
	 ColumnValuePair<T, ?>[] getColumnValuePairs() {
		return columnValuePairs;
	}
	 Predicate<? super T> getWhere() {
		return where;
	}
	
	@Override
	public  void executeOn(Database database) {
		database.execute(this);
	}
	
	
	
	
}
