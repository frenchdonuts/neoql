package net.ericaro.neoql;

import java.util.HashMap;

public class MapMapper<S, T> extends HashMap<S, T> implements Mapping<S, T> {

	Mapper<S,T> mapper ;
	
	public MapMapper(Mapper<S, T> mapper) {
		super();
		this.mapper = mapper;
	}

	@Override
	public T pop(S s) {
		return get(s);
	}

	@Override
	public T push(S s) {
		T t = mapper.map(s);
		put(s,t);
		return t;
	}


	@Override
	public T peek(S s) {
		return get(s);
	}
	
	
	
}
