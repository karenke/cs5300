package org.myorg;

import java.io.IOException;
import java.util.Iterator;
import java.lang.Math;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class BlockedPageRankCalculateReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	
	private static final double damping = 0.85F;
    
	public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		// Hashtable of <page_ID, PR(u)> for pages in block_u -- for residual calculation
		Hashtable<String, Double> oldRankValues = new Hashtable<String, Double> ();
		// Hashtable of <page_ID, edges from page_ID> for pages in block_u -- for output
    	Hashtable<String, String> linksTable = new Hashtable<String, String> ();
    	
    	// Hashtable of <page_ID, pages with edges from the same the block >
    	// for all pages in block_u with edges from  the same block
    	Hashtable<String, ArrayList<String> > BE = new Hashtable<String, ArrayList<String> > ();
    	// Hashtable of <page_ID, degree of the page> -- only nodes in BE has degrees
    	Hashtable<String, Integer> degreeTable = new Hashtable<String, Integer> ();
    	
    	// Hashtable of <page_ID, PR's from other blocks>
    	// for all pages in block_u with edges from other block
    	Hashtable<String, ArrayList<Double>> BC = new Hashtable<String, ArrayList<Double>> ();
    	
    	String currentValue;
        while (values.hasNext()) {
        	currentValue = values.next().toString();
        	// "|" denote: node "\t" links
        	if (currentValue.startsWith("|")) {
        		int linkIndex = currentValue.indexOf("\t");
        		String node_u = currentValue.substring(1, linkIndex);
        		String links_u = currentValue.substring(linkIndex + 1);
        		linksTable.put(node_u, links_u);
        		continue;
        	}
        	// "!" denote: node "\t" PR
        	if (currentValue.startsWith("!")) {
        		int prIndex = currentValue.indexOf("\t");
        		String node_u = currentValue.substring(1, prIndex);
        		Double oldRank = Double.parseDouble(currentValue.substring(prIndex + 1));
        		oldRankValues.put(node_u, oldRank);
        		continue;
        	}
        	// Otherwise, it's in the format: page_u "\t" PR/n "\t" from page_v+block_v "\t" degree
        	int nodeIndex = currentValue.indexOf("\t");
        	// Node u
        	String page_u = currentValue.substring(0, nodeIndex);
        	int rankIndex = currentValue.indexOf("\t", nodeIndex + 1);
        	// Rank from the edge
        	double rank = Double.parseDouble(currentValue.substring(nodeIndex + 1, rankIndex));
        	int pageAndBlockIndex = currentValue.indexOf("\t", rankIndex + 1);
        	// From node v
        	String page_and_block_v = currentValue.substring(rankIndex + 1, pageAndBlockIndex);
        	int pageIndex = page_and_block_v.indexOf("+");
        	String page_v = page_and_block_v.substring(0, pageIndex);
        	String block_v = page_and_block_v.substring(pageIndex + 1);
        	// Degree of v
        	int degree_v = Integer.parseInt(currentValue.substring(pageAndBlockIndex + 1));
        	
        	// If u, v from the same block, add it into BE and record the degree
        	if (block_v.equals(key.toString())) {
        		if (BE.containsKey(page_u)) {
        			ArrayList<String> list = BE.get(page_u);
        			list.add(page_v);
        			BE.put(page_u, list);
        		}
        		else {
            		ArrayList<String> list = new ArrayList<String>();
            		list.add(page_v);
            		BE.put(page_u, list);
            	}
        		degreeTable.put(page_v, degree_v);
        	}
        	// If they are from different block, just record the boundary value because this value is fixed
        	else {
        		if (BC.containsKey(page_u)) {
        			ArrayList<Double> list = BC.get(page_u);
        			list.add(rank / degree_v);
        			BC.put(page_u, list);
        		}
        		else {
            		ArrayList<Double> list = new ArrayList<Double>();
            		list.add(rank / degree_v);
            		BC.put(page_u, list);
            	}
        	}
        }
        
        Hashtable<String, Double> PR = new Hashtable<String, Double> (oldRankValues);
        double localResidual = PR.size();
        int numIter = 0;
        // Calculate new page rank value
        while (localResidual > 0.001 * PR.size()) {
        	//System.out.println("---------Calculate------------");
        	++ numIter;
        	/*-------------------DEBUG--------------------------*/
        	//if (numIter == 2) break;
        	/*-------------------DEBUG--------------------------*/
            localResidual = 0.0;
        	// One iteration, refer to pesudo-code on handout
        	Hashtable<String, Double> NPR = new Hashtable<String, Double> (PR);
        	Iterator NPR_setup_iter = NPR.keySet().iterator();
        	while (NPR_setup_iter.hasNext()) {
        		String tempPage = NPR_setup_iter.next().toString();
        		NPR.put(tempPage, new Double(0.0));
        	}
        	
        	// u belongs to Block
        	Iterator BE_page_iter = BE.keySet().iterator();
        	while (BE_page_iter.hasNext()) {
        		String page_v = BE_page_iter.next().toString();
        		ArrayList<String> edges_in_block = BE.get(page_v);
        		double pr_value = 0;
        		for (String page_u : edges_in_block) {
        			double pr = PR.get(page_u);
        			int dgr = degreeTable.get(page_u);
    				// Only one case that source node has 0 income degree: dead end node
        			//pr_value += (dgr == 0) ? pr : pr/dgr;
        			pr_value += (dgr == 0) ? 0 : pr/dgr;
        		}
       			NPR.put(page_v, new Double(pr_value));
        	}
        	
        	// u not belong to Block
        	Iterator BC_page_iter = BC.keySet().iterator();
        	while (BC_page_iter.hasNext()) {
        		String page_v = BC_page_iter.next().toString();
        		ArrayList<Double> edges_out_block = BC.get(page_v);
        		double pr_value = 0;
        		//System.out.println("Trying to find " + page_v);
        		//System.out.println("In " + NPR.toString());
        		//System.out.println("Current Block: " + key.toString());
        		System.out.println("Page_v_id: " + page_v);
        		pr_value = NPR.get(page_v);
        		for (Double R : edges_out_block) {
        			pr_value += R.doubleValue();
        		}
        		NPR.put(page_v, pr_value);
        	}
        	
        	// Damping factor
        	Iterator NPR_page_iter = NPR.keySet().iterator();
        	while (NPR_page_iter.hasNext()) {
        		String page_v = NPR_page_iter.next().toString();
        		double pr = NPR.get(page_v);
        		//System.out.println( "Values: " + pr + "; for page: " + page_v);
        		pr = damping * pr + (1 - damping);// / BlockedPageRank.N;
        		//System.out.println("Damped Value: " + pr + "; for page: " + page_v);
        		NPR.put(page_v, pr);
        	}
        	
        	// Update PR and calculate localResidual
        	NPR_page_iter = NPR.keySet().iterator();
        	while (NPR_page_iter.hasNext()) {
        		String page_v = NPR_page_iter.next().toString();
        		double pr = NPR.get(page_v);
        		//System.out.println(page_v);
        		//System.out.println(PR.toString());
        		localResidual += Math.abs(PR.get(page_v) - pr) / pr;
        		//System.out.println("old value: " + PR.get(page_v));
        		//System.out.println("Addon of localResidual: " + Math.abs(PR.get(page_v) - pr) / pr);
        		PR.put(page_v, new Double(pr));
        	}
        	//System.out.println("localResidual: " + localResidual);
            //System.out.println("Block Size " + PR.size() + "; localResidual " + localResidual);
        }
        

        
       	double residualValue = 0.0;
       	Iterator PR_page_iter = PR.keySet().iterator();
    	while (PR_page_iter.hasNext()) {
    		String page_v = PR_page_iter.next().toString();
    		double pr = PR.get(page_v);
    		double old_pr = oldRankValues.get(page_v);
           	residualValue += Math.abs(pr - old_pr) / pr;
    	}
    	
        //System.out.println("Residual: " + residualValue);
        long amplifiedValue = (long)Math.round(residualValue * 10e6);
        reporter.incrCounter(MyCounter.GLOBAL_RESIDUAL_COUNTER, amplifiedValue);
        reporter.incrCounter(MyCounter.LOCAL_ITERATION_NUM_COUNTER, numIter);
        // System.out.println("numIter: " + numIter);
        reporter.incrCounter(MyCounter.BLOCK_COUNTER, 1);
        
        // Compose output for reducer: key- nodeID+blockID; value- PR '\t' nodeID+blockID_1,nodeID+blockID_2,....
        PR_page_iter = PR.keySet().iterator();
    	while (PR_page_iter.hasNext()) {
    		String page_id = PR_page_iter.next().toString();
    		String page_and_block = page_id + "+" + key.toString();
    		double pr = PR.get(page_id);
    		String links = linksTable.get(page_id);
    		output.collect(new Text(page_and_block), new Text(pr + "\t" + links));
    	}
    	
    	// Get the highest pagerank value in the block
    	double highestRank = Collections.max(PR.values());
    	int currentBlock = Integer.parseInt(key.toString());
    	long highestValueLong = (long)Math.round(highestRank * 10e6);
    	reporter.incrCounter(HighestRankCounter.values()[currentBlock], highestValueLong);
    	// System.out.println("Highest PageRank Value in Block: " + HighestRankCounter.values()[currentBlock] + " is: " + highestRank);
    }
}







