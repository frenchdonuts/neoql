package net.ericaro.osql.system;


public class CreateTable implements Operation<Void> {

	Class table;

	public CreateTable(Class table) {
		super();
		this.table = table;
	}

	@Override
	public Void run(Database database) {
		database.run(this);
		return null;
	}

	Class getTable() {
		return table;
	}

}
