package net.ericaro.neoql.changeset;

import java.util.Map.Entry;

import net.ericaro.neoql.Column;
import net.ericaro.neoql.CreateTableChange;
import net.ericaro.neoql.DeleteChange;
import net.ericaro.neoql.DropTableChange;
import net.ericaro.neoql.InsertChange;
import net.ericaro.neoql.UpdateChange;
import net.ericaro.neoql.tables.Pair;

public class Changes {
	
	public static String toString(Change change) {
		final StringBuilder sb = new StringBuilder() ;
		change.accept(new ChangeVisitor() {
			
			@Override
			public void changed(DropTableChange change) {
				for(Pair<Class, Column[]> c: change.dropped())
					sb.append("DROP TABLE ").append(c.getLeft().getSimpleName()).append(";\n");
			}
			
			@Override
			public void changed(CreateTableChange change) {
				for(Pair<Class, Column[]> c: change.created()) {
					sb.append("CREATE TABLE ").append(c.getLeft().getSimpleName()).append("(\n");
					for(Column col : c.getRight())
						sb.append("\t").append(col).append("\n");
					sb.append(");\n");
				}
			}
			
			@Override
			public <T> void changed(UpdateChange<T> change) {
				
				sb.append("UPDATE TABLE ").append(change.getType().getSimpleName()).append(" VALUES (\n");
				for(Entry<T, T> e: change.updates())
					sb.append("\t").append(e.getKey()).append(" -> ").append(e.getValue()).append(";\n");
				sb.append(");\n");
			}
			
			
			@Override
			public <T> void changed(InsertChange<T> change) {
				sb.append("INSERT INTO ").append(change.getType().getSimpleName()).append("( \n");
				for(T t: change.inserted())
					sb.append("\t").append(t).append("\n");
				sb.append(");\n");
			}
			
			@Override
			public <T> void changed(DeleteChange<T> change) {
				sb.append("DELETE FROM ").append(change.getType().getSimpleName()).append("( \n");
				for(T t: change.deleted())
					sb.append("\t").append(t).append("\n");
				sb.append(");\n");
			}
			
			@Override
			public void changed(ChangeSet change) {
				for(Change c: change.changes())
					c.accept(this);
			}
		});
		return sb.toString() ;
	}

}
