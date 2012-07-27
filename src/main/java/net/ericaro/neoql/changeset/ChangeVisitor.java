package net.ericaro.neoql.changeset;

import net.ericaro.neoql.CreateCursorChange;
import net.ericaro.neoql.CreateTableChange;
import net.ericaro.neoql.DeleteChange;
import net.ericaro.neoql.DropCursorChange;
import net.ericaro.neoql.DropTableChange;
import net.ericaro.neoql.InsertChange;
import net.ericaro.neoql.PropertyChange;
import net.ericaro.neoql.UpdateChange;

public interface ChangeVisitor {

	
	void changed(ChangeSet change);
	<T> void changed(DeleteChange<T> change);
	<T> void changed(InsertChange<T> change);
	<T> void changed(PropertyChange<T> change);
	<T> void changed(UpdateChange<T> change);
	void changed(DropCursorChange dropCursorChange);
	void changed(CreateCursorChange createCursorChange);
	void changed(CreateTableChange createTableChange);
	void changed(DropTableChange dropTableChange);
	
	
}
