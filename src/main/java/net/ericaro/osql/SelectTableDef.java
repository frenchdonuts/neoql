package net.ericaro.osql;

public class SelectTableDef<T> implements TableDef<T> {

	Select<T> select;

	public SelectTableDef(Select<T> select) {
		super();
		this.select = select;
	}

	public Select<T> getSelect() {
		return select;
	}

	@Override
	public Table<T> asTable(Database database) {
		return database.table(select);
	}

	
}
