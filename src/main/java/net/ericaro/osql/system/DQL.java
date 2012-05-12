package net.ericaro.osql.system;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;

public class DQL {

	public static <T> Insert<T> insertInto(Class<T> table) {
		return new Insert<T>(table);
	}

	public static <T> Update<T> update(Class<T> table) {
		return new Update<T>(table);
	}

	public static <T> DeleteFrom<T> deleteFrom(Class<T> table) {
		return new DeleteFrom<T>(table);
	}

	public static <T> Select<T> select(Class<T> table, Where<? super T> p) {
		return new Select<T>(table, p);
	}
	
	public static <T,V> Where<T> columnIs(final Column<T,V> col, final V value){
		return new Where<T>() {

			@Override
			public boolean isTrue(T t) {
				if (value == null) return false; // null is always false
				return value.equals(col.get(t) ) ;
			}
			
		};
	}
	
	public static <T> Column<T,?>[] columnsOf(Class<T> tableClass) {
		List<Column<T,?>> cols = new ArrayList<Column<T,?>>();
		try {
			for (Field f : tableClass.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) && Modifier.isStatic(mod)
						&& Modifier.isPublic(mod)
						&& f.get(null) instanceof Column) {
					// I should write stuff in the col object, it will be reused
					Column<T,?> col = (Column<T,?>) f.get(null);
					if (col.field == null) {// not init
						col.field = tableClass.getDeclaredField( col.fname);
						col.field.setAccessible(true);
					}
					cols.add(col);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cols.toArray(new Column[cols.size()]);
	}

}
