package net.ericaro.neoql;

public interface ContentVisitor {

	<T> void visit(ContentTable<T> table);
	<T> void visit(Cursor<T> table);
	
	
}
