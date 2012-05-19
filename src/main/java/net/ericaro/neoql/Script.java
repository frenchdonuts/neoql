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
public class Script extends NeoQL implements Statement{

	private List<Statement> statements = new ArrayList<Statement>();
	
	protected <T> CreateTable<T> createTable(ClassTableDef<T> c) {
		return exec(new CreateTable<T>(c));
	}
	protected <T> CreateProperty<T> createProperty(ClassTableDef<T> c, Property<T> prop) {
		return exec(new CreateProperty<T>(c, prop));
	}
	protected <T> DropProperty<T> dropProperty(Property<T> prop) {
		return exec(new DropProperty<T>(prop));
	}

	protected <T> InsertInto<T> insertInto(ClassTableDef<T> table) {
		return exec(new InsertInto<T>(table));
	}

	protected <T> Update<T> update(ClassTableDef<T> table) {
		return exec(new Update<T>(table));
	}

	protected <T> DeleteFrom<T> deleteFrom(ClassTableDef<T> table) {
		return exec(new DeleteFrom<T>(table));
	}
	
	protected <T> PropertyValue<T> put(Property<T> prop, T value) {
		return exec(new PropertyValue<T>(prop, value));
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
