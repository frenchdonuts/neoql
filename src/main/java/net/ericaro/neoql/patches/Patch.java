package net.ericaro.neoql.patches;

public interface Patch {
	<U> U accept(PatchVisitor<U> v);
}
