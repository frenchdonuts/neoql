package net.ericaro.neoql.notinvasive;

public class Stuff {

	
	private String name;
	private int id;
	boolean selected = true;
	
	public String getName() {
		return name;
	}
	public int getId() {
		return id;
	}
	@Override
	public String toString() {
		return (selected?"* ":"- ")+name + " (" + id + ")";
	}
	public boolean isSelected() {
		return selected;
	}

	
}
