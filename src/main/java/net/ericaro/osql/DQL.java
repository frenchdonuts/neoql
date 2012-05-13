package net.ericaro.osql;

/**
 * EDSL for the Data Query Language
 * 
 * @author eric
 * 
 */
public class DQL {

	public static Predicate<Object> True = new Predicate<Object>() {
		@Override
		public boolean eval(Object t) {
			return true;
		}
	};

	public static <T, V> Predicate<T> columnIs(final Column<T, V> col,
			final V value) {
		return new Predicate<T>() {

			@Override
			public boolean eval(T t) {
				if (value == null)
					return false; // null is always false
				return value.equals(col.get(t));
			}

		};
	}
	
	

	public static <T> InsertInto<T> insertInto(Class<T> table) {
		return new InsertInto<T>(table);
	}

	public static <T> Update<T> update(Class<T> table) {
		return new Update<T>(table);
	}

	public static <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return new DeleteFrom<T>(table);
	}

	public static <T> CreateTable<T> createTable(Class<T> table) {
		return new CreateTable<T>(table);
	}
	
	public static <T> TableDef<T> select(TableDef<T> table, Predicate<? super T> where){
		return new SelectTableDef<T>(new Select<T>(table, where));
	}
	public static <T> TableDef<T> select(Class<T> table, Predicate<? super T> where){
		return new SelectTableDef<T>(new Select<T>(table, where));
	}
	

}
