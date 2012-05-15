package net.ericaro.osql;

 class InnerJoin<L, R>  implements TableDef<Pair<L,R>> {

	
	TableDef<L> leftTable;
	TableDef<R> rightTable;
	Predicate<? super Pair<L,R> > on ;
	 InnerJoin(TableDef<L> left, TableDef<R> right,
			Predicate<? super Pair<L,R>> on) {
		super();
		this.leftTable = left;
		this.rightTable = right;
		this.on = on;
	}
	 TableDef<L> getLeftTable() {
		return leftTable;
	}
	 TableDef<R> getRightTable() {
		return rightTable;
	}
	 Predicate<? super Pair<L, R>> getOn() {
		return on;
	}
	@Override
	public Table<Pair<L, R>> asTable(Database database) {
		return database.table(this);
	}
	
	
}
