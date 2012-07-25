package net.ericaro.neoql.properties;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Cursor;
import net.ericaro.neoql.Table;

public class Tracker {
	
	public static <T> Property<T> track(Table<T> table, T value) {
		return new ObservableCursor<T>(table);
	}

	public static <T, C> ColumnProperty<T, C> track(Property<T> source, Column<T, C> column) {
		return new ColumnProperty<T, C>(source, column);
	}
}
