package net.ericaro.osql.projectors;

import net.ericaro.osql.system.Projector;

public class All implements Projector{

	@Override
	public Object[] project(Object[] next) {
		return next;
	}

	
	
	
	
}
