package org.myorg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class BlockedPageRankCalculateMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{    
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		// Parse input to find: page_u+block_u
    	int page_and_block_index = value.find("\t");
    	String page_and_block = Text.decode(value.getBytes(), 0, page_and_block_index);
    	// Parse page_u+block_u to find: page_u, and block_u
    	int block_index = page_and_block.indexOf("+");
    	
    	String page_u = page_and_block.substring(0, block_index);
    	String block_u = page_and_block.substring(block_index + 1, page_and_block.length());
    	// Parse input to find: page rank of page_u+blcok_u
    	int rank_index = value.find("\t", page_and_block_index + 1);
    	Double rank = Double.parseDouble(Text.decode(value.getBytes(), page_and_block_index + 1, rank_index - (page_and_block_index+1)));

    	String links = "";
    	// Parse input to find: link pages of u - v1, v2, ...
    	if (value.getLength() > (rank_index + 1)) {
    		links = Text.decode(value.getBytes(), rank_index + 1, value.getLength() - (rank_index + 1));
        	String[] otherPages = links.split(",");
        	
        	// Degree of page u
        	int degree = otherPages.length;
        	
        	for (String page_and_block_v : otherPages) {
        		// Parse page_v+block_v to find: page_v, and block_v
            	int block_v_index = page_and_block_v.indexOf("+");
            	String page_v = page_and_block_v.substring(0, block_v_index);
            	String block_v = page_and_block_v.substring(block_v_index + 1, page_and_block_v.length());
            	// Create intermediate pairs: (b_v1; v1 PR b_u), (b_v2; v2 PR b_u) ...
        		Text value_for_page_v = new Text(page_v + "\t" + rank + "\t" + page_and_block + "\t" + degree);
        		output.collect(new Text(block_v), value_for_page_v);
        		//System.out.println(block_v + " $ " + value_for_page_v);
        	}
    	}
    	// No output links then consider it has links to every node in the graph
    	else {
    		output.collect(new Text(block_u), new Text(page_u + "\t" + rank + "\t" + page_and_block + "\t" + 0));
    		//System.out.println(block_u + " $ " + page_u + "\t" + rank + "\t" + page_and_block + "\t" + 0);
    	}
    	
    	// Create intermediate pairs: (page_u+block_u; |v1+b1,v2+b2,v3+b3...)
		// "|" is the control char denoting it's links rather than PR
		output.collect(new Text(block_u), new Text("|" + page_u + "\t" + links));
		//System.out.println(block_u + "|" + page_u + "\t" + links);
		// "!" is the control char denoting it's a node with the old PR
		output.collect(new Text(block_u), new Text("!" + page_u + "\t" + rank));
		//System.out.println(block_u + "!" + page_u + "\t" + rank);
    }
}

