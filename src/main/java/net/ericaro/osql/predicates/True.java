package net.ericaro.osql.predicates;

import net.ericaro.osql.system.Where;

public class True implements Where<Object> {

	@Override
	public boolean isTrue(Object o) {
		return true;
	}

}
