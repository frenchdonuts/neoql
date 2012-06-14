package net.ericaro.neoql.lang;

import net.ericaro.neoql.system.Attribute;
import net.ericaro.neoql.system.Column;
import net.ericaro.neoql.system.Pair;
import net.ericaro.neoql.system.Predicate;

/**
 * The column implementation. Separated from the interface to avoid references to it in the client's model.
 * @author eric
 * 
 * @param <T>
 * @param <V>
 */
class ColumnDef<T, V> implements Column<T, V> {

	// the foreign class definition, null if there is no foreign class definition
	private ClassTableDef<V>	foreignTable;
	Attribute<T, V>				attr;

	public ColumnDef(Attribute<T, V> attr, ClassTableDef<V> foreignTable) {
		super();
		this.attr = attr;
		this.foreignTable = foreignTable;
	}

	public ColumnDef(Attribute<T, V> attr) {
		this(attr, null);
	}

	/**
	 * Copies every columns values from src into target.
	 * 
	 * make use of the abstract get and set method to achieve it's goal.
	 * 
	 * @param src
	 * @param target
	 */
	void copy(T src, T target) {
		set(target, get(src));
	}

	@Override
	public V get(T src) {
		return attr.get(src);
	}
	
	void set(T src, V value) {
		attr.set(src, value);
	}

	@Override
	public V map(T source) {
		return get(source);
	}
	

	@Override
	public String getName() {
		return attr.getName();
	}

	public ClassTableDef<V> getForeignTable() {
		return foreignTable;
	}

	public boolean hasForeignKey() {
		return foreignTable != null;
	}


	public Predicate<T> is(final V value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false
				return value.equals(ColumnDef.this.get(t));
			}
			
			public String toString(){
				return ColumnDef.this.attr.getName()+" = "+value ;
			}

		};
	}

	/**
	 * if this columns has a foreign key, returns a predicate that is true if the pair left joins.
	 * for instance
	 * for a Pair<Student,Teacher> p, and this column is "Student.teacher" then
	 * p.getLeft().teacher = p.getRight()
	 * 
	 * 
	 * @return
	 */
	public Predicate<Pair<T, V>> joins() {
		return new Predicate<Pair<T, V>>() {

			@Override
			public boolean eval(Pair<T, V> t) {
				return get(t.getLeft()) == t.getRight();
			}
			public String toString(){
				return ColumnDef.this.attr.getName()+".id = that.id";
			}

		};
	}

	private Class<V> getType() {
		return attr.getType();
	}
	@Override
	public String toString() {
		return getName()+" "+getType().getSimpleName()+(foreignTable==null?"":" FOREIGN KEY REFERENCES "+foreignTable.getName());
	}


	
	
	
}
