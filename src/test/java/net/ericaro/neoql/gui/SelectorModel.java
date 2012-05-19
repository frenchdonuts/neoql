package net.ericaro.neoql.gui;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Database;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.Script;
import net.ericaro.neoql.TableDef;
import net.ericaro.neoql.TableList;

import static net.ericaro.neoql.NeoQL.*;
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

		public static Column<Person, String>	NAME		= new Column<Person, String>("name");
		public static Column<Person, Boolean>	SELECTED	= new Column<Person, Boolean>("selected");

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

	// ##########################################################################
	// ENTITY DEFINITION END
	// ##########################################################################

	// ##########################################################################
	// TABLES DEFINITION BEGIN
	// ##########################################################################
	
	public static TableDef<Person> SELECTED = select(Person.class, Person.SELECTED.is(true));
	public static TableDef<Person> UNSELECTED = select(Person.class, Person.SELECTED.is(false));
	public static TableDef<Person> ALL = select(Person.class);
	
	
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
				createTable(Person.class);
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
				insertInto(Person.class)
				.set(Person.NAME, name);
			}
		});
	}
	
	public void selectPerson(final Person p, final boolean selected) {
		db.execute(new Script() {
			{
				update(Person.class)
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
