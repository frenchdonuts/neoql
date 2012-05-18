package net.ericaro.neoql;


/**
 * a Drop Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class DropProperty<T> implements Statement {

	private Property<T> property;

	DropProperty(Property<T> property) {
		super();
		this.property = property;
	}

	public Property<T> getProperty() {
		return property;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
