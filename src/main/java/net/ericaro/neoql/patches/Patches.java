package net.ericaro.neoql.patches;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.Column;

public class Patches {
	public static String toString(Patch Patch) {
		return Patch.accept(new PatchVisitor<String>() {
			
			String str(Class type) {
				return type.getSimpleName();
			}
			
			@Override
			public String visit(DropTable patch) {
				return String.format("DROP TABLE %s ;", str(patch.getType())  );
			}
			
			@Override
			public String visit(CreateTable patch) {
				StringBuilder sb = new StringBuilder();
				for(Column col : patch.getColumns())
					sb.append("\t").append(col).append("\n");
				return String.format("CREATE TABLE %s (\n %s );", str(patch.getType()), sb.toString()  );
			}
			
			@Override
			public <T> String visit(Update<T> patch) {
				return String.format("UPDATE TABLE %s %s -> %s;", str(patch.getType()),   patch.getOldValue(), patch.getNewValue());
			}
			
			
			@Override
			public <T> String visit(Insert<T> patch) {
				return String.format("INSERT INTO %s %s ;", str(patch.getType()),   patch.getInserted());
			}
			
			@Override
			public <T> String visit(Delete<T> patch) {
				return String.format("DELETE FROM %s %s ;", str(patch.getType()),   patch.getDeleted());
			}
			
			@Override
			public String visit(PatchSet patch) {
				StringBuilder sb = new StringBuilder();
				for(Patch c: patch)
					sb.append(c.accept(this)).append("\n");
				return sb.toString();
			}
		});
	}


	public static Patch reverse(Patch Patch) {
		return Patch.accept(new PatchVisitor<Patch>() {
			
			@Override
			public Patch visit(DropTable patch) {
				return new CreateTable(patch.getType(), patch.getColumns());
			}
			
			@Override
			public Patch visit(CreateTable patch) {
				return new DropTable(patch.getType(), patch.getColumns());
			}
			
			@Override
			public <T> Patch visit(Update<T> patch) {
				return new Update<T>(patch.getType(), patch.getNewValue(), patch.getOldValue());
			}
			
			@Override
			public <T> Patch visit(Insert<T> patch) {
				return new Delete<T>(patch.getType(), patch.getInserted());
			}
			
			@Override
			public <T> Patch visit(Delete<T> patch) {
				return new Insert<T>(patch.getType(), patch.getDeleted());
			}
			
			@Override
			public Patch  visit(PatchSet patch) {
				List<Patch> reversed = new ArrayList<Patch>();
				for(Patch p: patch)
					reversed.add(0, p.accept(this) );
				
				return new PatchSet(reversed);
			}
		});
	}

	
}
