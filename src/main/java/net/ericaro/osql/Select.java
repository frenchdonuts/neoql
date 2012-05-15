package net.ericaro.osql;

/** SELECT * FROM table WHERE
 * 
 * @author eric
 *
 * @param <T>
 */
 class Select<T> {

	TableDef<T> table;
	Predicate<? super T> where;
	// TODO append sort, and group by

	 Select(Class<T> table, Predicate<? super T> where) {
		this(new ClassTableDef<T>(table) , where);
	}

	 Select(TableDef<T> table, Predicate<? super T> where) {
		super();
		this.table = table;
		this.where = where;
	}

	 TableDef<T> getTable() {
		return table;
	}


	 Predicate<? super T> getWhere() {
		return where;
	}

	 void setWhere(Predicate<? super T> where) {
		this.where = where;
	}
	
}
