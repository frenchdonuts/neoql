package net.ericaro.osql.system;

public class DeleteFrom<T> implements Operation<Void> {

	Class<T> table;
	private Where<? super T> where;
	
	
	public DeleteFrom(Class<T> table) {
		super();
		this.table = table;
	}

	public DeleteFrom<T> where(Where<? super T> where) {
		this.where = where;
		return this;
	}

	@Override
	public Void run(Database database) {
		database.run(this);
		return null;
	}

	Class<T> getTable() {
		return table;
	}

	Where<? super T> getWhere() {
		return where;
	}

	
	
}
