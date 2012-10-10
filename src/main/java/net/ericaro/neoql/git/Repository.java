package net.ericaro.neoql.git;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.ericaro.neoql.patches.Patch;
import net.ericaro.neoql.patches.PatchBuilder;
import net.ericaro.neoql.patches.Patches;
import net.ericaro.neoql.tables.Pair;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.algorithms.shortestpath.ShortestPathUtils;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Graphs;

/** represents a bare "git" like repository, i.e pure history, and totally stateless.
 * 
 * Method are left public only for the sake of writing stuff around git. Do not use as a "standard" user.
 * 
 * @author eric
 *
 */
public class Repository {
	DirectedGraph<Commit, Patch>  graph; // The actual history
	private Commit	root;
	
	/** creates a new empty repository.
	 * 
	 */
	public Repository() {
		graph = new DirectedSparseMultigraph<Commit, Patch>();
		graph.addVertex(root = new Commit("init"));
	}
	
	/** called by git client to record a patch (i.e a change) into this repository history.
	 * 
	 * @param change the patch to be used as edge
	 * @param from the starting edge
	 * @param comment the comment to associate with the commit
	 * @return
	 */
	public Commit commit(Patch change, Commit from, String comment) {
		Commit to = new Commit(comment);
		synchronized (graph) {
			graph.addEdge(change, from, to);
		}
		return to;
	}
	/** creates a merge commit from two points.
	 * 
	 * @param local the local change
	 * @param localHead head one 
	 * @param remote
	 * @param remoteHead
	 * @param comment
	 * @return
	 */
	public Commit merge(Commit common, Commit localHead, Commit remoteHead, Patch middle, String comment) {
		
		// simulates an "undo" up to the common ancestor, then from there apply the "middle" path
		PatchBuilder localTransaction = asPatchBuilder(localHead, common);
		localTransaction.apply(middle);// apply middle (i.e merge)
		Patch local = localTransaction.build();
		
		// idem from the remote point of view.
		PatchBuilder remoteTransaction = asPatchBuilder(remoteHead, common);
		remoteTransaction.apply(middle);
		Patch remote = remoteTransaction.build();
		
		// accept a single patch as input and computes the left part and the right part to guarantee that those
		// two patches are consistent, i.e. the commit is the same no matter the path
		
		Commit to = new Commit(comment);
		synchronized (graph) {
			graph.addEdge(local, localHead, to);
			graph.addEdge(remote, remoteHead, to);
		}
		return to;
	}

	
	/** Compute the shortest path between two commits.
	 * Cave at: Patches return are potentially "reversed" so that to checkout you only need to apply those path. 
	 * Another consequence is that potentially the Path returned does not belong to the graph.
	 * 
	 * @param from
	 * @param target
	 * @return
	 */
	public Iterable<Patch> path(Commit from, Commit target) {
		// creates a undirected graph to compute distances
		List<Patch> path = new ArrayList<Patch>();
		Commit temphead = from;
		
		List<Patch> rawPath = rawPath(from, target);
		
		for(Patch c: rawPath ) {
			// changes are correct, but the "order" is not deduced at all
			Commit next;
			Commit source;
			synchronized (graph) {
				next = graph.getOpposite(temphead, c);
				source = graph.getSource(c);
			}
			
			if(source.equals(temphead)) // the change is in the right order
            	path.add(c);
            else {
            	// change is backward
            	path.add(Patches.reverse(c) );
            }
			temphead = next;
            
		}
		assert temphead == target : "the computed path did not end into the target as expected";
		return path;
	}

	
	private List<Patch> rawPath(Commit from, Commit target) {
		SparseMultigraph<Commit, Patch> g = new SparseMultigraph<Commit, Patch>();
		
		List<Patch> rawPath;
		synchronized (graph) {
			
			for (Patch c : graph.getEdges())
				g.addEdge(c, graph.getEndpoints(c));
			
			DijkstraShortestPath<Commit, Patch> sp = new DijkstraShortestPath<Commit, Patch>(g) ;
			rawPath = ShortestPathUtils.getPath(graph, sp, from, target);
		
		}
		return rawPath;
	}
	
	private List<Commit> commitPath(Commit from, Commit target){
		List<Commit> path = new ArrayList<Commit>();
		path.add(from);
		
		synchronized (graph) {
			
			for (Patch p : rawPath(from, target))
				path.add(graph.getDest(p));
			
		}
		return path;
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
		
		//take shorcut for dummy changes
		if(from == target) 	return from;
		
		List<Commit> targetPre = commitPath(root, target);
		// taking the shortcut for fastforward
		if( targetPre.contains(from)) return target; // fast forward 
		
		// taking the shortcut for nothing to update 
		List<Commit> fromPre = commitPath(root, from);
		if ( fromPre.contains(target) ) 	return from; // nothing to update
		
		fromPre.retainAll(targetPre);
		assert fromPre.size()> 0: "there is no common ancestor, obviously";
		return fromPre.get(fromPre.size()-1);
		}
	/** use path instead
	 * 
	 * @param from
	 * @param target
	 * @return
	 */
	@Deprecated
	public Iterable<Patch> changePath(Commit from, Commit target){
		return path(from, target);
	}
	
	/** build a patch builder "along" the shortest path.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public PatchBuilder asPatchBuilder(Commit from, Commit to) {
		PatchBuilder builder = new PatchBuilder();
		for (Patch p : path(from, to ) )  
			builder.apply(p);
		return builder;
	}
	
	
	public String printPath(Branch from, Branch target) {
		return printPath(from.getCommit(), target.getCommit());
	}
	public String printPath(Commit from, Commit target) {
		StringBuilder sb = new StringBuilder();
		for(Patch p: changePath(from, target))
			sb.append(p).append("\n");
		return sb.toString();
	}

	public DirectedGraph<Commit, Patch> getGraph() {
		// TODO Auto-generated method stub
		return Graphs.unmodifiableDirectedGraph(graph);
	}
	

	public Commit getRoot() {
		return root;
	}
	
	
}