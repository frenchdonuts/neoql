package net.ericaro.neoql;

import net.ericaro.neoql.changeset.Change;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class Git {
	Graph<Commit, Change>  graph;
	static long idCount = 0L;
	Commit head ;
	public class Commit {
		long id = idCount++;
		String comment;
		public Commit(String comment) {
			super();
			this.comment = comment;
		}
		@Override
		public String toString() {
			return id + " [" + comment + "]";
		}
	}
	
	private Database	db;
	public Git(Database db) {
		graph = new DirectedSparseMultigraph<Commit, Change>();
		graph.addVertex(head=new Commit("init"));
		this.db = db;
	}
	
	public Commit commit() {return commit("");}
	public Commit commit(String comment) {
		graph.addEdge(db.commit() , head, head= new Commit(comment));
		return head;
	}
	
	public Commit tag() {
		return head;
	}
	
	public void checkout(Commit tag) {
		// creates a undirected graph to compute distances
		SparseMultigraph<Commit, Change> g = new SparseMultigraph<Commit, Change>();
		for(Change c: graph.getEdges())
			g.addEdge(c, graph.getEndpoints(c));
		DijkstraShortestPath<Commit, Change> sp = new DijkstraShortestPath<Commit, Change>(g) ;
		for(Change c: ShortestPathUtils.getPath(graph, sp, head, tag) ) {
			// changes are correct, but the "order" is not deduced at all
            if(graph.getSource(c).equals(head)) // the change is in the right order
            	db.apply(c);
            else // change is backward
            	db.apply(c.reverse() );
            head = graph.getOpposite(head, c);
		}
	}
}