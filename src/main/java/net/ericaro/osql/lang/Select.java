package net.ericaro.osql.lang;

/** SELECT * FROM table WHERE
 * 
 * @author eric
 *
 * @param <T>
 */
public class Select<T> {

	Class<T> table;
	Predicate<? super T> where;
	// TODO append sort, and group by
	
	public Select(Class<T> table, Predicate<? super T> where) {
		super();
		this.table = table;
		this.where = where;
	}

	public Class<T> getTable() {
		return table;
	}

	public void setTable(Class<T> table) {
		this.table = table;
	}

	public Predicate<? super T> getWhere() {
		return where;
	}

	public void setWhere(Predicate<? super T> where) {
		this.where = where;
	}
	
}
