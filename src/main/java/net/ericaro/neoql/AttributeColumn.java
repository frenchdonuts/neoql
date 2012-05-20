package net.ericaro.neoql;

public class AttributeColumn<T, V> extends AbstractColumn<T, V> {

	Attribute<T,V> attr;
	
	
	
	public AttributeColumn(Attribute<T, V> attr,ClassTableDef<V> foreignTable) {
		super(foreignTable);
		this.attr = attr;
	}
	public AttributeColumn(Attribute<T, V> attr) {
		super();
		this.attr = attr;
	}
	@Override
	public V get(T src) {
		return attr.get(src);
	}

	@Override
	void set(T src, V value) {
		attr.set(src, value);
	}

}
