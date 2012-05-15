package net.ericaro.neoql;

/** SELECT * FROM table WHERE
 * 
 * @author eric
 *
 * @param <T>
 */
 class MapSelect<S,T> extends Select<S>{

	Mapper<S,T> mapper;
	// TODO append sort, and group by

	 MapSelect(Mapper<S,T> mapper, Class<S> table, Predicate<? super S> where) {
		this(mapper, new ClassTableDef<S>(table) , where);
	}
	 
	 MapSelect(Mapper<S,T> mapper, TableDef<S> table, Predicate<? super S> where) {
		super(table, where);
		this.mapper = mapper;
	}

	public Mapper<S, T> getMapper() {
		return mapper;
	}
	 
	 
}
