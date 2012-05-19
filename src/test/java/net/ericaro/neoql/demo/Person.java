package net.ericaro.neoql.demo;

import net.ericaro.neoql.ClassTableDef;
import net.ericaro.neoql.Column;
import net.ericaro.neoql.NeoQL;

/**
 * 
 * @author eric
 *
 */
public class Person {
	public static final ClassTableDef<Person> TABLE       = new ClassTableDef<Person>(Person.class);
	
	public static final Column<Person, Long> ID           = TABLE.addColumn("id"); 
	public static final Column<Person, String> FIRST_NAME = TABLE.addColumn("firstName"); 
	public static final Column<Person, String> LAST_NAME  = TABLE.addColumn("lastName");
	public static final Column<Person, String> ADDRESS    = TABLE.addColumn("address"); 
	public static final Column<Person, String> CITY       = TABLE.addColumn("city"); 
	
	
	private Long id;
	private String firstName;
	private String lastName;
	private String address;
	private String city ;
	
	
	
	public long getId() {
		return id;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getAddress() {
		return address;
	}
	public String getCity() {
		return city;
	}
	@Override
	public String toString() {
		return "Person [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", address=" + address + ", city=" + city + "]";
	}
	
	
	
}
