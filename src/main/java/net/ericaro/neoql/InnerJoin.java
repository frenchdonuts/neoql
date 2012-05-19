package net.ericaro.neoql;

import java.util.Iterator;

import net.ericaro.neoql.InnerJoinTable.InnerJoinIterator;

public class InnerJoin<L, R> implements TableDef<Pair<L, R>> {

	TableDef<L> leftTable;
	TableDef<R> rightTable;
	Predicate<? super Pair<L, R>> on;

	public InnerJoin(TableDef<L> left, TableDef<R> right,
			Predicate<? super Pair<L, R>> on) {
		super();
		this.leftTable = left;
		this.rightTable = right;
		this.on = on;
	}

	public TableDef<L> getLeftTable() {
		return leftTable;
	}

	public TableDef<R> getRightTable() {
		return rightTable;
	}

	public Predicate<? super Pair<L, R>> getOn() {
		return on;
	}

	@Override
	public Table<Pair<L, R>> asTable(Database database) {
		return database.table(this);
	}

	@Override
	public Iterator<Pair<L, R>> iterator(Database database) {
		return new InnerJoinIterator<L,R>(
				database.iterator(leftTable),
				database.select(rightTable),
				on
				);
	}

	
}
