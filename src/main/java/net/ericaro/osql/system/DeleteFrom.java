package net.ericaro.osql.system;

public class DeleteFrom implements Operation<Void> {

	Class table;
	private Predicate where;
	
	
	public DeleteFrom(Class table) {
		super();
		this.table = table;
	}

	public DeleteFrom where(Predicate where) {
		this.where = where;
		return this;
	}

	@Override
	public Void run(Database database) {
		database.run(this);
		return null;
	}

	Class getTable() {
		return table;
	}

	Predicate getWhere() {
		return where;
	}

	
	
}
