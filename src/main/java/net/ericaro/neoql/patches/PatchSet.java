package net.ericaro.neoql.patches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PatchSet implements Patch, Iterable<Patch> {
	List<Patch> patches = new ArrayList<Patch>();
	
	public PatchSet(Patch... operation) {
		this(Arrays.asList(operation));
	}
	public PatchSet(Iterable<Patch> operation) {
		super();
		for (Patch o: operation)
		if(o !=null)
			patches.add(o);
	}

	@Override
	public Iterator<Patch> iterator() {
		return Collections.unmodifiableList(patches).iterator();
	}
	
	public boolean isEmpty() {
		return patches.isEmpty();
	}
	
	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

	public String toString() {
		return Patches.toString(this);
	}

}
