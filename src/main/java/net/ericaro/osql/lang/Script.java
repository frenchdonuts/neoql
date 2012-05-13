package net.ericaro.osql.lang;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.osql.system.Database;
import net.ericaro.osql.system.SelectList;

/**
 * Script made of a bunch of statements
 * @author eric
 *
 */
public class Script {

	List<Statement> statements = new ArrayList<Statement>();
	
	public <T> CreateTable<T> createTable(Class<T> c) {
		return schedule(DQL.createTable(c));
	}


	public <T> InsertInto<T> insertInto(Class<T> table) {
		return schedule(DQL.insertInto(table));
	}

	public <T> Update<T> update(Class<T> table) {
		return schedule(DQL.update(table));
	}

	public <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return schedule(DQL.deleteFrom(table));
	}
	
	private <T extends Statement> T schedule(T stm) {
		statements.add(stm);
		return stm;
	}
	
	
	public void executeOn(Database database) {
		for(Statement  stm: statements)
			stm.executeOn(database);
	}
}
