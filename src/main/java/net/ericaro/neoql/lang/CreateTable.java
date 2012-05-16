package net.ericaro.neoql.lang;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.Database;

/**
 * a Create Table Statement
 * 
 * @author eric
 * 
 * @param <T>
 */
public class CreateTable<T> implements Statement {

	private Class<T> table;
	private Column<T, ?>[] columns;

	private static <T> Column<T, ?>[] columnsOf(Class<T> tableClass) {
		List<Column<T, ?>> cols = new ArrayList<Column<T, ?>>();
		try {

			for (Field f : tableClass.getDeclaredFields()) {
				int mod = f.getModifiers();
				if (Modifier.isStatic(mod) && Modifier.isPublic(mod)
						&& f.get(null) instanceof Column) {
					// I should write stuff in the col object, it will be reused
					Column<T, ?> col = (Column<T, ?>) f.get(null);
					col.init(tableClass);
					cols.add(col);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cols.toArray(new Column[cols.size()]);
	}

	CreateTable(Class<T> table) {
		super();
		this.table = table;
		columns = columnsOf(table);
	}

	public Class<T> getTable() {
		return table;
	}

	public Column<T, ?>[] getColumns() {
		return columns;
	}

	@Override
	public void executeOn(Database database) {
		database.execute(this);
	}

}
