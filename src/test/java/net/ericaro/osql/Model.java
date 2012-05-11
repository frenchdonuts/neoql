package net.ericaro.osql;

import net.ericaro.osql.system.Column;

public class Model {

	public static class Student {
		
		public static final Column<Integer> a = new Column<Integer>();
		public static final Column<String> b = new Column<String>();
		
		private int _a ;
		private String _b;
		
		
		
		int getA() {
			return _a;
		}

		String getB() {
			return _b;
		}

		@Override
		public String toString() {
			return "Student [" + _a + ", " + _b + "]";
		}
		
		
		
	}
}
