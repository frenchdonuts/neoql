package net.ericaro.neoql.git;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
	
	public Commit merge(Patch local, Commit localHead, Branch branch, Patch remote, Commit remoteHead, String comment) {
		
		Commit to = new Commit(comment);
		graph.addEdge(local, localHead, to);
		graph.addEdge(remote, remoteHead, to);
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
	
	
	/** computes a common ancestor between two commits
	 * 
	 * if the common ancestor is from: then we are in a fastforward situation
	 * if the common ancestor is target: we are in a nothing to update situation
	 * 
	 * @param from
	 * @param target
	 * @return the common ancestor:
	 */
	public Commit commonAncestor(Commit from, Commit target) {
		if(from == target) {
			return from;
		}
		if ( graph.isPredecessor(from, target)) {
			// I'm ahead of the remote branch
			return from; // nothing to update
		}
		if ( graph.isPredecessor(target, from))
			return target; // fastfroward
		for( Commit c: graph.getPredecessors(from) )
			if ( graph.isPredecessor(target, c))
				return c;
		assert false: "there is no common ancestor, obviously";
		return null;
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