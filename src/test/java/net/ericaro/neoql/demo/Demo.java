package net.ericaro.neoql.demo;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.TableDef;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;

public class Demo {

	public static void main(String[] args) {

		// create the database
		Database db = new Database();

		// create an fill the Person Table
		db.execute(new Script() {
			{

				createTable(Person.class);

				insertInto(Person.class).set(Person.ID, 1L).set(Person.FIRST_NAME, "Hansen").set(Person.LAST_NAME, "Ola").set(Person.ADDRESS, "Timoteivn 10").set(Person.CITY, "Sandnes");
				insertInto(Person.class).set(Person.ID, 2L).set(Person.FIRST_NAME, "Svendson").set(Person.LAST_NAME, "Tove").set(Person.ADDRESS, "Borgvn 23").set(Person.CITY, "Sandnes");
				insertInto(Person.class).set(Person.ID, 3L).set(Person.FIRST_NAME, "Pettersen").set(Person.LAST_NAME, "Kari").set(Person.ADDRESS, "Storgt 20").set(Person.CITY, "Stavanger");
			}
		});
		
		System.out.println("Printing the current Person Table");
		for(Person p: db.select(Person.class)) // equivalent to select * from Person )
			System.out.println(p);
		
		// now SELECT firstName from Person 
		// statements creation, and statement execution can be separated, even more, the same statement can be executed on two different database instances.
		
		// this is the pure statement
		TableDef<String> selectName = NeoQL.select(Person.FIRST_NAME, Person.class);
		
		// to use it I need to apply it to a db:
		System.out.println("Select firstName from Person => ");
		for(String p: db.select(selectName)) // equivalent to select * from Person )
			System.out.println(p);
		
		// TODO select distinct 
		
		System.out.println("SELECT * FROM Persons WHERE City='Sandnes'");
		TableDef<Person> selectSandnes = NeoQL.select(Person.class, NeoQL.is(Person.CITY, "Sandnes"));
		for(Person p: db.select(selectSandnes)) // equivalent to select * from Person )
			System.out.println(p);
		
		
		
		
		
		

	}

}
