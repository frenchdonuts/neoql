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

	public static DeleteFrom deleteFrom(Class table) {
		return new DeleteFrom(table);
	}

	public static <T> Select<T> select(Class<T> table, Where<? super T> p) {
		return new Select<T>(table, p);
	}

	public static Column[] columnsOf(Class tableClass) {
		List<Column> cols = new ArrayList<Column>();
		try {
			for (Field f : tableClass.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) && Modifier.isStatic(mod)
						&& Modifier.isPublic(mod)
						&& f.get(null) instanceof Column) {
					// I should write stuff in the col object, it will be reused
					Column col = (Column) f.get(null);
					if (col.field == null) {// not init
						col.field = tableClass.getDeclaredField("_"
								+ f.getName());
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
