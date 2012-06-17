package net.ericaro.neoql;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ListModel;



/**
 * EDSL for the Data Query Language
 * 
 * @author eric
 * 
 */
public class NeoQL {
	// TODO add javadoc in there, once the api is stable
	

	/** Simple 'true' predicate (always returns true)
	 * 
	 */
	public static Predicate<Object>	True	= new Predicate<Object>() {
												public boolean eval(Object t) {	return true; }
											};
											
	/** Simple 'false' predicate (always returns false)
	 * 
	 */
	public static Predicate<Object>	False	= new Predicate<Object>() {
												public boolean eval(Object t) { return false; }
											};
    
	/** creates a table definition
	 * 
	 * @param type
	 * @return
	 */
	public static final <T> ClassTableDef<T> table(Class<T> type){return new ClassTableDef<T>(type);}
											
	public static <T, V> Predicate<T> is(final Column<T, V> col, final V value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false
				return value.equals(col.get(t));
			}

		};
	}
	/**
	 * return the identity predicate for this type.
	 * 
	 * @param value
	 * @return
	 */
	public static <T> Predicate<T> is(final T value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false, is it ?
				return t == value;
			}
		};
	}

	
	public static <T, V> Predicate<T> in(final Column<T, V> col, V... value) {
		final Set<V> values = new HashSet<V>(Arrays.asList(value));
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return values.contains(col.get(t));
			}
		};
	}

	public static <T> Predicate<T> and(final Predicate<T> left, final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) && right.eval(t);
			}
		};
	}

	public static <T> Predicate<T> all(final Predicate<T> left, final Predicate<? super T>... right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				if (!left.eval(t))
					return false;
				for (Predicate p : right)
					if (!p.eval(t))
						return false;
				return true;
			}
		};
	}

	public static <T> Predicate<T> any(final Predicate<T> left, final Predicate<? super T>... right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				if (left.eval(t))
					return true;
				for (Predicate p : right)
					if (p.eval(t))
						return true;
				return false;
			}
		};
	}

	public static <T> Predicate<T> or(final Predicate<T> left, final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) || right.eval(t);
			}
		};
	}

	
	public static <T> SelectTable<T> where(Table<T> table, Predicate<T> where){
		return new SelectTable<>(table, where);
	}
	public static <S, T> MappedTable<S,T> map(Table<S> table,Mapper<S, T> mapper) {
		return new MappedTable<S, T>(mapper, table);
	}
	
	public static <S, T> Table<T> groupBy(Table<S> table, Column<S, T> groupBy) {
		return new GroupByTable<S, T>(groupBy, table);
	}
	
	public static <T, V extends Comparable<? super V>> Table<T> orderyBy(Table<T> table, Column<T, V> orderBy, boolean ascendent) {
		return new OrderByTable<T, V>(table,orderBy, ascendent);
	}
	
	public static <L, R> Table<Pair<L, R>> innerJoin(Table<L> leftTable, Table<R> rightTable, Predicate<? super Pair<L, R>> on) {
		return new InnerJoinTable<L, R>(leftTable, rightTable,  on );
	}

	public static <L,R> Table<L> left(Table<Pair<L,R>> table) {
		Mapper<Pair<L,R>, L> map= Pair.left() ;
		return map(table, map);
	}
	
	public static <L,R> Table<R> right(Table<Pair<L,R>> table) {
		Mapper<Pair<L,R>, R> map= Pair.right() ;
		return map(table, map);
	}
	
	public static <T> Iterable<T> select(final Table<T> table) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return table.iterator();
			}
		};
	}
	
	public static <T> Iterable<T> select(final Table<T> table, final Predicate<T> where) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SelectTable.SelectIterator<T>(table.iterator(), where);
			}
		};
		
		
	}
	
	
	public static <T, U extends ListModel&Iterable<T>> U listFor(Table<T> table) {
		return (U) new TableList<T>(table);
	}

}
