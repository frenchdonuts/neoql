package net.ericaro.osql;

/** SELECT * FROM table WHERE
 * 
 * @author eric
 *
 * @param <T>
 */
public class Select<T> {

	TableDef<T> table;
	Predicate<? super T> where;
	// TODO append sort, and group by

	public Select(Class<T> table, Predicate<? super T> where) {
		this(new ClassTableDef<T>(table) , where);
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

	public void setWhere(Predicate<? super T> where) {
		this.where = where;
	}
	
}
