package net.ericaro.neoql.git;

public class Branch {

	Commit state ;
	
	public Branch() {
	}
	
	void setCommit(Commit state) {
		this.state = state;
	}
	
	public Commit getCommit() {
		return state;
	}

}
