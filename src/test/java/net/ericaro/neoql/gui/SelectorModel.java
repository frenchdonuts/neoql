package net.ericaro.neoql.gui;

import static net.ericaro.neoql.NeoQL.select;
import net.ericaro.neoql.ClassTableDef;
import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Script;
import net.ericaro.neoql.TableDef;
import net.ericaro.neoql.TableList;
public class SelectorModel {

	// ##########################################################################
	// ENTITY DEFINITION BEGIN
	// ##########################################################################
	
	
	
	/**
	 * A single selectable person
	 * 
	 * @author eric
	 * 
	 */
	public static class Person {
		public static final ClassTableDef<Person> TABLE = new ClassTableDef<Person>(Person.class);

		public static Column<Person, String>	NAME		= TABLE.addColumn("name");
		public static Column<Person, Boolean>	SELECTED	= TABLE.addColumn("selected");
		

		private String			name;
		private boolean			selected = false;

		public String toString() {
			return "<html>" + (selected ? "<i>" : "") + name + (selected ? "</i>" : "") + "</html>";
		}
		
		public static Predicate<Person> is(final Person p) {
			return new Predicate<Person>() {
				public boolean eval(Person t) {
					return t == p;
				}};
		}

	}
	// summary of all available tables in this model
	public static final ClassTableDef<Person> PERSON = Person.TABLE;

	// ##########################################################################
	// ENTITY DEFINITION END
	// ##########################################################################

	// ##########################################################################
	// TABLES DEFINITION BEGIN
	// ##########################################################################
	
	public static TableDef<Person> SELECTED = select(PERSON, Person.SELECTED.is(true));
	public static TableDef<Person> UNSELECTED = select(PERSON, Person.SELECTED.is(false));
	public static TableDef<Person> ALL = select(PERSON);
	
	
	// ##########################################################################
	// TABLES DEFINITION END
	// ##########################################################################

	
	
	Database	db	= new Database();
	private TableList<Person>	selected;
	private TableList<Person>	unSelected;
	private TableList<Person>	allPersons;

	public SelectorModel() {

		db.execute(new Script() {
			{
				// INIT script, here its trivial
				createTable(PERSON);
			}
		});
		addPerson("Joe");
		addPerson("John");
		addPerson("Julia");
		addPerson("Jonathan");
		
		// creates the observable lists
		selected = db.listFor(SELECTED);
		unSelected = db.listFor(UNSELECTED);
		allPersons= db.listFor(ALL);

	}
	
	
	
	// OPERATIONS
	
	public void addPerson(final String name) {
		db.execute(new Script() {
			{
				insertInto(PERSON)
				.set(Person.NAME, name);
			}
		});
	}
	
	public void selectPerson(final Person p, final boolean selected) {
		db.execute(new Script() {
			{
				update(PERSON)
					.where(Person.is(p) )
				.set(Person.SELECTED, selected);
			}
		});
	}

	// ACCESSORS

	public TableList<Person> getSelected() {
		return selected;
	}



	public TableList<Person> getUnSelected() {
		return unSelected;
	}



	public TableList<Person> getAllPersons() {
		return allPersons;
	}
}
