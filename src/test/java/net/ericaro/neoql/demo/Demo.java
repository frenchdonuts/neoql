package net.ericaro.neoql.demo;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.InsertInto;
import net.ericaro.neoql.NeoQL;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Script;
import net.ericaro.neoql.TableDef;

public class Demo {
	public static final Property<Person> CURRENT = new Property<Person>();

	public static void main(String[] args) {

		// create the database
		Database db = new Database();

		System.out.println("\n\n");
		System.out.println("Creating and filling the database");
		// create an fill the Person Table
		db.execute(new Script() {
			{

				createTable(Person.class);

				insertInto(Person.class).set(Person.ID, 1L).set(Person.LAST_NAME, "Hansen"   ).set(Person.FIRST_NAME, "Ola").set(Person.ADDRESS, "Timoteivn 10").set(Person.CITY, "Sandnes");
				insertInto(Person.class).set(Person.ID, 2L).set(Person.LAST_NAME, "Svendson" ).set(Person.FIRST_NAME, "Tove").set(Person.ADDRESS, "Borgvn 23").set(Person.CITY, "Sandnes");
				insertInto(Person.class).set(Person.ID, 3L).set(Person.LAST_NAME, "Pettersen").set(Person.FIRST_NAME, "Kari").set(Person.ADDRESS, "Storgt 20").set(Person.CITY, "Stavanger");
				insertInto(Person.class).set(Person.ID, 4L).set(Person.LAST_NAME, "Nilsen"   ).set(Person.FIRST_NAME, "Tom").set(Person.ADDRESS, "Vingvn 23").set(Person.CITY, "Stavanger");
			}
		});
		
		for(Person p: db.select(Person.class)) // equivalent to select * from Person )
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("Select firstName from PERSON");
		
		// statements creation, and statement execution can be separated, even more, the same statement can be executed on two different database instances.
		
		// this is the pure statement
		TableDef<String> selectName = NeoQL.select(Person.FIRST_NAME, Person.class);
		// to use it I need to apply it to a db:
		for(String p: db.select(selectName)) // equivalent to select * from Person )
			System.out.println(p);
		
		// TODO select distinct 
		
		System.out.println("\n\n");
		System.out.println("SELECT * FROM Persons WHERE City='Sandnes'");
		TableDef<Person> selectSandnes = NeoQL.select(Person.class, NeoQL.is(Person.CITY, "Sandnes"));
		for(Person p: db.select(selectSandnes)) // equivalent to select * from Person )
			System.out.println(p);
		
		
		
		System.out.println("\n\n");
		System.out.println("SELECT * FROM Persons ORDER BY LastName");
		TableDef<Person> selectsortedBy= NeoQL.select(Person.class, Person.LAST_NAME, true);
		for(Person p: db.select(selectsortedBy)) // equivalent to select * from Person )
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("INSERT INTO Persons VALUES (5,'Nilsen', 'Johan', 'Bakken 2', 'Stavanger')");
		
		db.execute(new Script() {{
			insertInto(Person.class).set(Person.ID, 5L).set(Person.LAST_NAME, "Nilsen"   ).set(Person.FIRST_NAME, "Johan").set(Person.ADDRESS, "Baken 2").set(Person.CITY, "Stavanger");
		}});
		for (Person p : db.select(Person.class))
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("INSERT INTO Persons (P_Id, LastName, FirstName) VALUES (6, 'Tjessem', 'Jakob')");
		
		db.execute(new Script() {{
			insertInto(Person.class).set(Person.ID, 6L).set(Person.LAST_NAME, "Tjessem").set(Person.FIRST_NAME, "Jakob");
		}});
		for (Person p : db.select(Person.class))
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("creates a property");
		db.execute(new Script() {{
			createProperty(Person.class, CURRENT);
		}});
		System.out.println("current is ");
		System.out.println(db.get(CURRENT));
		
		TableDef<Person> selector = NeoQL.select(Person.class, NeoQL.is(Person.ID, 4L));
		
		Iterator<Person> i = db.iterator(selector);
		i.hasNext();// there is a bug in my iterator, it needs a call to hasnext first sorry
		final Person current = i.next();
		System.out.println("\n\ncurrent person will be "+current);
		db.execute(new Script() {{
			put(CURRENT, current);
		}});
		System.out.println("\n\ncurrent is ");
		System.out.println(db.get(CURRENT));

		System.out.println("\n\nupdating the selected person in the database");
		db.execute(new Script() {{
			update(Person.class).where( is(Person.ID, 4L) ).set(Person.FIRST_NAME, "toto");
		}});
		
		System.out.println("current is ");
		System.out.println(db.get(CURRENT));
		

		
		

	}

}
