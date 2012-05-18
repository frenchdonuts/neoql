package net.ericaro.neoql.demo;

import net.ericaro.neoql.Column;

/**
 * 
 * @author eric
 *
 */
public class Person {
	
	public static final Column<Person, Long> ID = new Column<Person, Long>("id"); 
	public static final Column<Person, String> FIRST_NAME = new Column<Person, String>("firstName"); 
	public static final Column<Person, String> LAST_NAME  = new Column<Person, String>("lastName");
	public static final Column<Person, String> ADDRESS    = new Column<Person, String>("address"); 
	public static final Column<Person, String> CITY       = new Column<Person, String>("city"); 
	
	
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
