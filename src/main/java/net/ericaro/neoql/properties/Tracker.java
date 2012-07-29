package net.ericaro.neoql.properties;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.Property;
import net.ericaro.neoql.Table;
import net.ericaro.neoql.tables.Mapper;

/** Shortcuts to create tracked properties.
 * 
 * @author eric
 *
 */
public class Tracker {
	
	public static <T> Property<T> track(Table<T> table, T value) {
		return new ObservableCursor<T>(table, value);
	}

	public static <T, C> ColumnProperty<T, C> track(Property<T> source, Column<T, C> column) {
		return new ColumnProperty<T, C>(source, column);
	}
	public static <T, C> Property<C> track(Property<T> source, Class<C> target, Mapper<T, C> mapper) {
		return new MappedProperty<T, C>(source, target, mapper);
	}
}
