package net.ericaro.osql.system;

interface Operation<T> {

	
	public T run(Database database);
}
