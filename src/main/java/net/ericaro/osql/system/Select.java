package net.ericaro.osql.system;

import java.util.List;

import javax.swing.ListModel;




public class Select<T> implements Operation<SelectList<T>>{

	Class<T> table;
	Where<? super T>  where;
	
	
	
	public Select(Class<T> table, Where<? super T>  where) {
		super();
		this.table = table;
		this.where = where;
	}



	public SelectList<T> run(Database database){
		return database.run(this);
	};

	
}
