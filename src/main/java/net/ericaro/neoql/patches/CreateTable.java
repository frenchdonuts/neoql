package net.ericaro.neoql.patches;

import net.ericaro.neoql.Column;

public class CreateTable implements Patch {

	protected Class key;
	protected Column[] columns;
	
	
	
	public CreateTable(Class key, Column... columns) {
		super();
		this.key = key;
		this.columns = columns;
	}

	public Class getType() {
		return key;
	}

	public Column[] getColumns() {
		return columns;
	}

	@Override
	public <U> U accept(PatchVisitor<U> v) {return  v.visit(this);}

}
