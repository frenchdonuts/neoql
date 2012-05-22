package net.ericaro.neoql.lang;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.system.Predicate;
import net.ericaro.neoql.system.Statement;


/**
 * Delete From class statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DeleteFrom<T> implements Statement {

	ClassTableDef<T> table;
	private Predicate<? super T> where;

	DeleteFrom(ClassTableDef<T> table) {
		super();
		this.table = table;
	}

	DeleteFrom(ClassTableDef<T> table, Predicate<? super T> where) {
		this(table);
		this.where = where;
	}

	public DeleteFrom<T> where(Predicate<? super T> where) {
		this.where = where;
		return this;
	}

	public ClassTableDef<T> getTable() {
		return table;
	}

	public Predicate<? super T> getWhere() {
		return where;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

	@Override
	public String toString() {
		return "DELETE FROM " + table.getName() + where ;
	}
	
}
