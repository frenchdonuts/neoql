package net.ericaro.neoql.changeset;

public interface ChangeVisitor {

	
	public void changed(ChangeSet change);
	public void changed(DeleteChange change);
	public void changed(InsertChange change);
	public void changed(PropertyChange change);
	public void changed(ReverseChange change);
	public void changed(UpdateChange change);
	
	
}
