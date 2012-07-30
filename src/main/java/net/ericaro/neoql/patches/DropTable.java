package net.ericaro.neoql.patches;

import net.ericaro.neoql.Column;

public class DropTable implements Patch {

	protected Class type;
	protected Column[] columns;
	
	
	
	public DropTable(Class type, Column... columns) {
		super();
		this.type = type;
		this.columns = columns;
	}

	public Class getType() {
		return type;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

}
