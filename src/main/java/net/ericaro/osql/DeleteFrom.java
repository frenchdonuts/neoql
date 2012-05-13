package net.ericaro.osql;


/**
 * Delete From class statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DeleteFrom<T> implements Statement {

	Class<T> table;
	private Predicate<? super T> where;

	public DeleteFrom(Class<T> table) {
		super();
		this.table = table;
	}

	public DeleteFrom(Class<T> table, Predicate<? super T> where) {
		this(table);
		this.where = where;
	}

	public DeleteFrom<T> where(Predicate<? super T> where) {
		this.where = where;
		return this;
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
	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
}
