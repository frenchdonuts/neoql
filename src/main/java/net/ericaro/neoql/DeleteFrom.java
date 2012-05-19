package net.ericaro.neoql;


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

	DeleteFrom(Class<T> table) {
		super();
		this.table = table;
	}

	DeleteFrom(Class<T> table, Predicate<? super T> where) {
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

	public Predicate<? super T> getWhere() {
		return where;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
}
