package net.ericaro.neoql;

public interface Column<T, V> extends Mapper<T, V> {

	
	public abstract V get(T src);
	public Predicate<T> is(final V value);
	/** if this columns has a foreign key, returns a predicate that is true if the pair left joins.
	  * for instance
	  * for a Pair<Student,Teacher> p, and this column is "Student.teacher" then
	  * p.getLeft().teacher = p.getRight()
	  * 
	  * 
	  * @return
	  */
	 public Predicate<Pair<T,V>> joins();
}
