package net.ericaro.neoql;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.ericaro.neoql.tables.GroupByTable;
import net.ericaro.neoql.tables.InnerJoinTable;
import net.ericaro.neoql.tables.MappedTable;
import net.ericaro.neoql.tables.Mapper;
import net.ericaro.neoql.tables.OrderByTable;
import net.ericaro.neoql.tables.Pair;
import net.ericaro.neoql.tables.SelectTable;





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
	public static Predicate<?>	True	= new Predicate<Object>() {
												public boolean eval(Object t) {	return true; }
											};
											
	/** Simple 'false' predicate (always returns false)
	 * 
	 */
	public static Predicate<?>	False	= new Predicate<Object>() {
												public boolean eval(Object t) { return false; }
											};
    
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

	/** return true if the column value is in the set of values
	 * 
	 * @param col
	 * @param value
	 * @return
	 */
	public static <T, V> Predicate<T> in(final Column<T, V> col, V... value) {
		final Set<V> values = new HashSet<V>(Arrays.asList(value));
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return values.contains(col.get(t));
			}
		};
	}

	/** returns a predicate that is <code>left AND right</code>
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static <T> Predicate<T> and(final Predicate<T> left, final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) && right.eval(t);
			}
		};
	}

	/** returns a predicate that is true iif all predicate are true (generalization of and)
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
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

	/** return a predicate that returns true iif one at least of the predicate is true.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
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

	/** returns a predicate that returns true if one of left or right returns true.
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static <T> Predicate<T> or(final Predicate<T> left, final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) || right.eval(t);
			}
		};
	}

	

	/**
	 * creates a columns by using introspection to access the field, and defines a foreign key
	 * 
	 * @param name
	 * @param columnType
	 * @return
	 */
	public static <T,V> Column<T, V> column(Class<T> type, String name, Class<V> columnType, boolean hasForeignKey) {
		IntrospectionAttribute<T, V> attr = new IntrospectionAttribute<T, V>(type, name);
		return column(type, attr, columnType, hasForeignKey);
	}

	/** creates a column by using introspection and does not define any foreign key
	 * 
	 * @param type
	 * @param name
	 * @param columnType
	 * @return
	 */
	public static <T,V> Column<T, V> column(Class<T> type, String name, Class<V> columnType) {
		IntrospectionAttribute<T, V> attr = new IntrospectionAttribute<T, V>(type, name);
		return column(type, attr, columnType, false);
	}
	
		
	/**
	 * Add a column by using the attribute accessor
	 * 
	 * @param attr
	 * @param columnType
	 * @return
	 */
	public static <T,V> Column<T, V> column(Class<T> type, Attribute<T, V> attr, Class<V> columnType, boolean hasForeignKey) {
		return new MyColumn<T, V>(type, attr, columnType,hasForeignKey);
	}
	
	/** creates a Select Table from another table and a predicate.
	 * 
	 * @param table
	 * @param where
	 * @return
	 */
	public static <T> SelectTable<T> where(Table<T> table, Predicate<T> where){
		return new SelectTable<T>(table, where);
	}
	/** creates a table using a transformation from the source table.
	 * Turns a table of S into a table of T provided that you give a Mapper<S,T> implementation. 
	 * 
	 * @param table
	 * @param mapper
	 * @return
	 */
	public static <S, T> MappedTable<S,T> map(Table<S> table,Mapper<S, T> mapper) {
		return new MappedTable<S, T>(mapper, table);
	}
	
	/** returns  table with a single colum of type T, so that T is a column of table<S> and t values are unique.
	 * 
	 * @param table
	 * @param groupBy
	 * @return
	 */
	public static <S, T> Table<T> groupBy(Table<S> table, Column<S, T> groupBy) {
		return new GroupByTable<S, T>(groupBy, table);
	}
	
	/** return a table ordered by a column
	 * 
	 * @param table
	 * @param orderBy
	 * @param ascendent
	 * @return
	 */
	public static <T, V extends Comparable<? super V>> Table<T> orderyBy(Table<T> table, Column<T, V> orderBy, boolean ascendent) {
		return new OrderByTable<T, V>(table,orderBy, ascendent);
	}
	/** creates an inner join table.
	 * 
	 * @param leftTable
	 * @param rightTable
	 * @param on
	 * @return
	 */
	public static <L, R> Table<Pair<L, R>> innerJoin(Table<L> leftTable, Table<R> rightTable, Predicate<? super Pair<L, R>> on) {
		return new InnerJoinTable<L, R>(leftTable, rightTable,  on );
	}
	
	/** split and inner join table into it's left counter part (usefull if you really need inner join to operation filter, or order)
	 * 
	 * @param table
	 * @return
	 */
	public static <L,R> Table<L> left(Table<Pair<L,R>> table) {
		Mapper<Pair<L,R>, L> map= Pair.left() ;
		return map(table, map);
	}
	
	/** same as left but for the right counterpart of the inner join.
	 * 
	 * @param table
	 * @return
	 */
	public static <L,R> Table<R> right(Table<Pair<L,R>> table) {
		Mapper<Pair<L,R>, R> map= Pair.right() ;
		return map(table, map);
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
	public static <L,R> Predicate<Pair<L, R>> joins(final Column<L,R> column) {
		return new Predicate<Pair<L, R>>() {
			@Override
			public boolean eval(Pair<L, R> pair) {
				return column.get( pair.getLeft() ) == pair.getRight();
			}
			public String toString(){
				return "this.id = that.id";
			}

		};
	}
	
	/** returns a simple iterator over a table. 
	 * 
	 * @param table
	 * @return
	 */
	public static <T> Iterable<T> select(final Table<T> table) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return table.iterator();
			}
		};
	}
	
	/** returns a simple iterator over a table, filtered by predicate == True
	 * 
	 * @param table
	 * @param where
	 * @return
	 */
	public static <T> Iterable<T> select(final Table<T> table, final Predicate<T> where) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return new SelectTable.SelectIterator<T>(table.iterator(), where);
			}
		};
	}

	/** return 
	 * if a is null, return true if b is null.
	 * otherwise return a.equals(b).
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static <T> boolean eq(T a, T b) {
			if (a == null)
				return b == null; 
			else
				return a.equals(b);
			
	}
}
