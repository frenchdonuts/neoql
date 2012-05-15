package net.ericaro.neoql;

/**
 * Delete From class statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DeleteFrom<T> implements Statement {

	Class<T>						table;
	private Predicate<? super T>	where;

	DeleteFrom(Class<T> table) {
		super();
		this.table = table;
	}

	DeleteFrom(Class<T> table, Predicate<? super T> where) {
		this(table);
		this.where = where;
	}

	DeleteFrom<T> where(Predicate<? super T> where) {
		this.where = where;
		return this;
	}

	Class<T> getTable() {
		return table;
	}

	void setTable(Class<T> table) {
		this.table = table;
	}

	Predicate<? super T> getWhere() {
		return where;
	}

	void setWhere(Predicate<? super T> where) {
		this.where = where;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}
}
