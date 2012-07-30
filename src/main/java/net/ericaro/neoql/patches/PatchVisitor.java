package net.ericaro.neoql.patches;


public interface PatchVisitor<U> {

	
	U visit(PatchSet patch);
	<T> U visit(Delete<T> patch);
	<T> U visit(Insert<T> patch);
	<T> U visit(Update<T> patch);
	
	U visit(CreateTable patch);
	U visit(DropTable patch);
	
	
}
