package net.ericaro.neoql.git;

import java.util.ArrayList;
import java.util.List;

import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.Patches;
import net.ericaro.neoql.tables.Pair;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Graphs;

/** represents a bare "git" like repository, i.e the pure history, and totally stateless.
 * 
 * @author eric
 *
 */
public class Repository {
	DirectedGraph<Commit, Patch>  graph; // The actual history
	private Commit	root;
	
	public Repository() {
		graph = new DirectedSparseMultigraph<Commit, Patch>();
		graph.addVertex(root = new Commit("init"));
	}
	
	public Commit commit(Patch change, Commit from, Branch branch, String comment) {
		Commit to = new Commit(comment);
		graph.addEdge(change, from, to);
		if (branch !=null ) branch.setCommit(to);
		return to;
	}
	
	public List<Pair<Patch, Commit>> path(Commit from, Commit target) {
		// creates a undirected graph to compute distances
		SparseMultigraph<Commit, Patch> g = new SparseMultigraph<Commit, Patch>();
		for(Patch c: graph.getEdges())
			g.addEdge(c, graph.getEndpoints(c));
		DijkstraShortestPath<Commit, Patch> sp = new DijkstraShortestPath<Commit, Patch>(g) ;
		List<Pair<Patch, Commit>> rawpath = new ArrayList<Pair<Patch, Commit>>();
		Commit temphead = from;
		for(Patch c: ShortestPathUtils.getPath(graph, sp, from, target) ) {
			// changes are correct, but the "order" is not deduced at all
			Commit next = graph.getOpposite(temphead, c);
			if(graph.getSource(c).equals(temphead)) // the change is in the right order
            	rawpath.add(new Pair<Patch, Commit>(c, next));
            else {
            	// change is backward
            	System.out.println("using path backward");
            	rawpath.add(new Pair<Patch, Commit>(Patches.reverse(c), next));
            }
			temphead = next;
            
		}
		assert temphead == target : "the computed path did not end into the target as expected";
		return rawpath;
	}
	
	public List<Patch> changePath(Commit from, Commit target){
		List<Patch> changes = new ArrayList<Patch>();
		for(Pair<Patch, Commit>  p: path(from,target))
			changes.add(p.getLeft());
		return changes;
	}

	public DirectedGraph<Commit, Patch> getGraph() {
		// TODO Auto-generated method stub
		return Graphs.unmodifiableDirectedGraph(graph);
	}
	

	public Commit getRoot() {
		return root;
	}

	
	// TODO look for create a common ancestor lookup !
	
	
}