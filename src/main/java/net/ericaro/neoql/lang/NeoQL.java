package net.ericaro.neoql.lang;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.ericaro.neoql.Mapper;
import net.ericaro.neoql.Pair;
import net.ericaro.neoql.Predicate;
import net.ericaro.neoql.TableDef;

/**
 * EDSL for the Data Query Language
 * 
 * @author eric
 * 
 */
public class NeoQL {

	public static Predicate<Object> True = new Predicate<Object>() {
		@Override
		public boolean eval(Object t) {
			return true;
		}
	};

	public static Predicate<Object> False = new Predicate<Object>() {
		@Override
		public boolean eval(Object t) {
			return false;
		}
	};

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

	public static <T, V> Predicate<T> in(final Column<T, V> col, V... value) {
		final Set<V> values = new HashSet<V>(Arrays.asList(value));
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return values.contains(col.get(t));
			}
		};
	}

	public static <T> Predicate<T> and(final Predicate<T> left,
			final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) && right.eval(t);
			}
		};
	}

	public static <T> Predicate<T> all(final Predicate<T> left,
			final Predicate<? super T>... right) {
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

	public static <T> Predicate<T> any(final Predicate<T> left,
			final Predicate<? super T>... right) {
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

	public static <T> Predicate<T> or(final Predicate<T> left,
			final Predicate<? super T> right) {
		return new Predicate<T>() {
			@Override
			public boolean eval(T t) {
				return left.eval(t) || right.eval(t);
			}
		};
	}

	public static <T> TableDef<T> select(Class<T> table) {
		return new Select<T>(table, NeoQL.True);
	}

	public static <T> TableDef<T> select(TableDef<T> table) {
		return new Select<T>(table, NeoQL.True);
	}

	public static <T> TableDef<T> select(Class<T> table,
			Predicate<? super T> where) {
		return new Select<T>(table, where);
	}

	public static <T> TableDef<T> select(TableDef<T> table,
			Predicate<? super T> where) {
		return new Select<T>(table, where);
	}

	public static <S, T> TableDef<T> select(Mapper<S, T> mapper, Class<S> table) {
		return new MapSelect<S, T>(mapper, table, NeoQL.True);
	}

	public static <S, T> TableDef<T> select(Mapper<S, T> mapper,
			TableDef<S> table) {
		return new MapSelect<S, T>(mapper, table, NeoQL.True);
	}

	public static <S, T> TableDef<T> select(Mapper<S, T> mapper,
			Class<S> table, Predicate<? super S> where) {
		return new MapSelect<S, T>(mapper, table, where);
	}

	public static <S, T> TableDef<T> select(Mapper<S, T> mapper,
			TableDef<S> table, Predicate<? super S> where) {
		return new MapSelect<S, T>(mapper, table, where);
	}

	public static <S, T> TableDef<T> select(Class<S> table, Column<S, T> groupBy) {
		return new GroupBySelect<S, T>(table, NeoQL.True, groupBy);
	}

	public static <S, T> TableDef<T> select(TableDef<S> table,
			Column<S, T> groupBy) {
		return new GroupBySelect<S, T>(table, NeoQL.True, groupBy);
	}

	public static <S, T> TableDef<T> select(TableDef<S> table,
			Predicate<? super S> where, Column<S, T> groupBy) {
		return new GroupBySelect<S, T>(table, where, groupBy);
	}

	public static <S, T> TableDef<T> select(Class<S> table,
			Predicate<? super S> where, Column<S, T> groupBy) {
		return new GroupBySelect<S, T>(table, where, groupBy);
	}

	public static <L, R> TableDef<Pair<L, R>> innerJoin(Class<L> left,
			Class<R> right, Predicate<? super Pair<L, R>> on) {
		return new InnerJoin<L, R>(left, right, on);
	}

	public static <L, R> TableDef<Pair<L, R>> innerJoin(TableDef<L> left,
			TableDef<R> right, Predicate<? super Pair<L, R>> on) {
		return new InnerJoin<L, R>(left, right, on);
	}
}
