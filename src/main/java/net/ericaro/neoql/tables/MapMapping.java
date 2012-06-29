package net.ericaro.neoql.tables;

import java.util.HashMap;

/** java.util.HashMap backed implementation of a Mapping
 * 
 * @author eric
 *
 * @param <S>
 * @param <T>
 */
public class MapMapping<S, T> extends HashMap<S, T>  {

	Mapper<S,T> mapper ;
	
	public MapMapping(Mapper<S, T> mapper) {
		super();
		this.mapper = mapper;
	}

	public T push(S s) {
		T t = mapper.map(s);
		put(s,t);
		return t;
	}


	
}
