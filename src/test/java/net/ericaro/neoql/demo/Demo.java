package net.ericaro.neoql.demo;

import java.util.Iterator;

import net.ericaro.neoql.Database;
import net.ericaro.neoql.lang.ClassTableDef;
import net.ericaro.neoql.lang.NeoQL;
import net.ericaro.neoql.lang.Script;
import net.ericaro.neoql.system.Property;
import net.ericaro.neoql.system.Table;
import net.ericaro.neoql.system.TableDef;
import net.ericaro.neoql.system.TableListener;

public class Demo {
	
	static ClassTableDef<Person> PERSON = Person.TABLE;
	
	public static final class LogObserver<T> implements TableListener<T> {
		
		private String	label;

		public LogObserver(String label) {
			super();
			this.label = label;
		}

		@Override
		public void updated(T oldRow, T newRow) {
			System.out.println("updated "+label+": \nwas: "+oldRow+"\n is : "+newRow);
		}

		@Override
		public void inserted(T newRow) {
			System.out.println("inserted "+label+": \n is: "+newRow);
		}

		@Override
		public void deleted(T oldRow) {
			System.out.println("deleted "+label+": \n is: "+oldRow);
		}
	}

	public static final Property<Person> CURRENT = new Property<Person>();

	public static void main(String[] args) {

		// create the database
		Database db = new Database();

		System.out.println("\n\n");
		System.out.println("Creating and filling the database");
		// create an fill the Person Table
		db.execute(new Script() {
			{

				createTable(PERSON);

				insertInto(PERSON).set(Person.ID, 1L).set(Person.LAST_NAME, "Hansen"   ).set(Person.FIRST_NAME, "Ola").set(Person.ADDRESS, "Timoteivn 10").set(Person.CITY, "Sandnes");
				insertInto(PERSON).set(Person.ID, 2L).set(Person.LAST_NAME, "Svendson" ).set(Person.FIRST_NAME, "Tove").set(Person.ADDRESS, "Borgvn 23").set(Person.CITY, "Sandnes");
				insertInto(PERSON).set(Person.ID, 3L).set(Person.LAST_NAME, "Pettersen").set(Person.FIRST_NAME, "Kari").set(Person.ADDRESS, "Storgt 20").set(Person.CITY, "Stavanger");
				insertInto(PERSON).set(Person.ID, 4L).set(Person.LAST_NAME, "Nilsen"   ).set(Person.FIRST_NAME, "Tom").set(Person.ADDRESS, "Vingvn 23").set(Person.CITY, "Stavanger");
			}
		});
		
		for(Person p: db.select(PERSON)) // equivalent to select * from Person )
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("Select firstName from PERSON");
		
		// statements creation, and statement execution can be separated, even more, the same statement can be executed on two different database instances.
		
		// this is the pure statement
		TableDef<String> selectName = NeoQL.select(Person.FIRST_NAME, PERSON);
		// to use it I need to apply it to a db:
		for(String p: db.select(selectName)) // equivalent to select * from Person )
			System.out.println(p);
		
		// TODO select distinct 
		
		System.out.println("\n\n");
		System.out.println("SELECT * FROM Persons WHERE City='Sandnes'");
		TableDef<Person> selectSandnes = NeoQL.select(PERSON, Person.CITY.is("Sandnes") );
		for(Person p: db.select(selectSandnes)) // equivalent to select * from Person )
			System.out.println(p);
		
		
		
		System.out.println("\n\n");
		System.out.println("SELECT * FROM Persons ORDER BY LastName");
		TableDef<Person> selectsortedBy= NeoQL.select(PERSON, Person.LAST_NAME, true);
		for(Person p: db.select(selectsortedBy)) // equivalent to select * from Person )
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("INSERT INTO Persons VALUES (5,'Nilsen', 'Johan', 'Bakken 2', 'Stavanger')");
		
		db.execute(new Script() {{
			insertInto(PERSON).set(Person.ID, 5L).set(Person.LAST_NAME, "Nilsen"   ).set(Person.FIRST_NAME, "Johan").set(Person.ADDRESS, "Baken 2").set(Person.CITY, "Stavanger");
		}});
		for (Person p : db.select(PERSON))
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("INSERT INTO Persons (P_Id, LastName, FirstName) VALUES (6, 'Tjessem', 'Jakob')");
		
		db.execute(new Script() {{
			insertInto(PERSON).set(Person.ID, 6L).set(Person.LAST_NAME, "Tjessem").set(Person.FIRST_NAME, "Jakob");
		}});
		for (Person p : db.select(PERSON))
			System.out.println(p);
		
		System.out.println("\n\n");
		System.out.println("creates a property");
		db.execute(new Script() {{
			createProperty(PERSON, CURRENT);
		}});
		System.out.println("current is ");
		System.out.println(db.get(CURRENT));
		
		TableDef<Person> selector = NeoQL.select(PERSON, Person.ID.is( 4L));
		
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
			update(PERSON).where( is(Person.ID, 4L) ).set(Person.FIRST_NAME, "toto");
		}});
		
		System.out.println("current is ");
		System.out.println(db.get(CURRENT));
		
		
		System.out.println("observability demo");
		System.out.println("it is possible to observe any query");
		TableDef<Person> stavangers = NeoQL.select(PERSON, Person.CITY.is("Stavanger" ));
		Table<Person> stavangersTable = db.tableFor(stavangers);
		System.out.println("initially strnvangers were ");
		for (Person p : stavangersTable)
			System.out.println(p);
		
		
		
		System.out.println("adding some observability");
		stavangersTable.addTableListener(new LogObserver<Person>("stavangers"));
		
		Table<String> cities = db.tableFor(NeoQL.select(PERSON, Person.CITY));
		cities.addTableListener(new LogObserver<String>("city") );
		System.out.println("initially cities were");
		for(String city : cities)
			System.out.println("city: "+city);
		
		System.out.println("insert some stavanger");
		db.execute(new Script() {{
			insertInto(PERSON)
				.set(Person.ID, 7L)
				.set(Person.FIRST_NAME, "Olaf")
				.set(Person.LAST_NAME, "Splaf")
				.set(Person.ADDRESS, "Nowhere 20")
				.set(Person.CITY, "Stavanger");
			insertInto(PERSON)
			.set(Person.ID, 8L)
				.set(Person.FIRST_NAME, "Olafson")
				.set(Person.LAST_NAME, "Splaf")
				.set(Person.ADDRESS, "Nowhere 22")
				.set(Person.CITY, "Stavanger");
			
		}});
		
		System.out.println("deleting some stavanger");
		db.execute(new Script() {{
			deleteFrom(PERSON)
			.where(Person.ID.is( 7L))
			;
			update(PERSON)
				.where(Person.ID.is( 8L))
				.set(Person.ADDRESS, "Somewhere 24")
			;
			update(PERSON)
			.where(Person.ID.is( 8L))
			.set(Person.CITY, "New York")
			;
			
			update(PERSON)
			.where(Person.ID.is( 8L))
			.set(Person.CITY, "Stavanger")
			;
			
		}});
		
		

	}

}
