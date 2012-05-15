package net.ericaro.neoql;

 class MapSelectTableDef<S,T> implements TableDef<T> {

	MapSelect<S,T> select;

	 MapSelectTableDef(MapSelect<S,T> select) {
		super();
		this.select = select;
	}

	 MapSelect<S,T> getSelect() {
		return select;
	}

	@Override
	public  Table<T> asTable(Database database) {
		return database.table(select);
	}

	
}
