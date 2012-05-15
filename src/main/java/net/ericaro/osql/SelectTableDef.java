package net.ericaro.osql;

 class SelectTableDef<T> implements TableDef<T> {

	Select<T> select;

	 SelectTableDef(Select<T> select) {
		super();
		this.select = select;
	}

	 Select<T> getSelect() {
		return select;
	}

	@Override
	public  Table<T> asTable(Database database) {
		return database.table(select);
	}

	
}
