package net.ericaro.neoql.git;

public class Branch {

	Commit state ;
	
	public Branch(Commit state) {
		this.state = state;;
	}
	
	void setCommit(Commit state) {
		this.state = state;
	}
	
	public Commit getCommit() {
		return state;
	}

	@Override
	public String toString() {
		return state.comment;
	}
	

}
