package net.ericaro.neoql.lang;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.Database;


/**
 * Script made of a bunch of statements
 * 
 * Use as an EDSL starter.
 * <code>
   db.execute(new Script() {{
			createTable(Test.class);
		}});
 * </code>
 * 
 * Note that script instance are stateless, they can be reused any time on any database
 * 
 * @author eric
 *
 */
public class Script implements Statement{

	private List<Statement> statements = new ArrayList<Statement>();
	
	protected <T> CreateTable<T> createTable(Class<T> c) {
		return exec(new CreateTable<T>(c));
	}

	protected <T> InsertInto<T> insertInto(Class<T> table) {
		return exec(new InsertInto<T>(table));
	}

	protected <T> Update<T> update(Class<T> table) {
		return exec(new Update<T>(table));
	}

	protected <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return exec(new DeleteFrom<T>(table));
	}
	
	protected <T extends Statement> T exec(T stm) {
		statements.add(stm);
		return stm;
	}
	
	
	/** part of the visitor pattern
	 * 
	 * @param database
	 */
	public void executeOn(Database database) {
		for(Statement  stm: statements)
			stm.executeOn(database);
	}
}
