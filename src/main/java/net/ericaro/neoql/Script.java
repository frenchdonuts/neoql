package net.ericaro.neoql;

import java.util.ArrayList;
import java.util.List;


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
public class Script {

	private List<Statement> statements = new ArrayList<Statement>();
	
	protected <T> CreateTable<T> createTable(Class<T> c) {
		return schedule(new CreateTable<T>(c));
		
		
		
	}


	protected <T> InsertInto<T> insertInto(Class<T> table) {
		return schedule(new InsertInto<T>(table));
	}

	protected <T> Update<T> update(Class<T> table) {
		return schedule(new Update<T>(table));
	}

	protected <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return schedule(new DeleteFrom<T>(table));
	}
	
	protected <T> TableDef<T> table(Class<T> table){
		return new ClassTableDef<T>(table);
	}
	
	protected <T> TableDef<T> select(TableDef<T> table, Predicate<? super T> where){
		return new SelectTableDef<T>(new Select<T>(table, where));
	}
	
	private <T extends Statement> T schedule(T stm) {
		statements.add(stm);
		return stm;
	}
	
	
	/** part of the visitor pattern
	 * 
	 * @param database
	 */
	void executeOn(Database database) {
		for(Statement  stm: statements)
			stm.executeOn(database);
	}
}
