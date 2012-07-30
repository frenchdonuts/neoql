package net.ericaro.neoql.git;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.ericaro.neoql.changeset.Change;
import net.ericaro.neoql.tables.Pair;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Graphs;

/** represents a bare "git" like repository, i.e the pure history, and totally stateless.
 * 
 * @author eric
 *
 */
public class Repository {
	DirectedGraph<Commit, Change>  graph; // The actual history
	private Commit	root;
	
	public Repository() {
		graph = new DirectedSparseMultigraph<Commit, Change>();
		graph.addVertex(root = new Commit("init"));
	}
	
	public Commit commit(Change change, Commit from, Branch branch, String comment) {
		Commit to = new Commit(comment);
		graph.addEdge(change, from, to);
		if (branch !=null ) branch.setCommit(to);
		return to;
	}
	
	public List<Pair<Change, Commit>> path(Commit from, Commit target) {
		// creates a undirected graph to compute distances
		SparseMultigraph<Commit, Change> g = new SparseMultigraph<Commit, Change>();
		for(Change c: graph.getEdges())
			g.addEdge(c, graph.getEndpoints(c));
		DijkstraShortestPath<Commit, Change> sp = new DijkstraShortestPath<Commit, Change>(g) ;
		List<Pair<Change, Commit>> rawpath = new ArrayList<Pair<Change, Commit>>();
		Commit temphead = from;
		for(Change c: ShortestPathUtils.getPath(graph, sp, from, target) ) {
			// changes are correct, but the "order" is not deduced at all
			Commit next = graph.getOpposite(temphead, c);
			if(graph.getSource(c).equals(temphead)) // the change is in the right order
            	rawpath.add(new Pair<Change, Commit>(c, next));
            else {
            	// change is backward
            	System.out.println("using path backward");
            	rawpath.add(new Pair<Change, Commit>(c.reverse(), next));
            }
			temphead = next;
            
		}
		assert temphead == target : "the computed path did not end into the target as expected";
		return rawpath;
	}
	
	public List<Change> changePath(Commit from, Commit target){
		List<Change> changes = new ArrayList<Change>();
		for(Pair<Change, Commit>  p: path(from,target))
			changes.add(p.getLeft());
		return changes;
	}

	public DirectedGraph<Commit, Change> getGraph() {
		// TODO Auto-generated method stub
		return Graphs.unmodifiableDirectedGraph(graph);
	}
	

	public Commit getRoot() {
		return root;
	}

	
	// TODO look for create a common ancestor lookup !
	
	
}