package org.myorg;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class PageRankCalculateMap extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text>{    
	public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
		// Parse input to find: page u
    	int page_index = value.find("\t");
    	String page_u = Text.decode(value.getBytes(), 0, page_index);
    	
    	// Parse input to find: page rank of u
    	int rank_index = value.find("\t", page_index + 1);
    	Double rank = Double.parseDouble(Text.decode(value.getBytes(), page_index + 1, rank_index - (page_index+1)));
    	
    	String links = "";
    	// Parse input to find: link pages of u - v1, v2, ...
    	if (value.getLength() > (rank_index + 1)) {
    		links = Text.decode(value.getBytes(), rank_index + 1, value.getLength() - (rank_index + 1));
        	String[] otherPages = links.split(",");
        	
        	// Degree of page u
        	int degree = otherPages.length;
        	
        	for (String page_v : otherPages) {
            	// Create intermediate pairs: (v1, PR), (v2, PR) ...
        		Text rank_value = new Text("" + rank/degree);
        		output.collect(new Text(page_v), rank_value);
        	}
    	}
    	/*else {
    		output.collect(new Text(page_u), new Text("" + rank));
    	}*/
    	
    	// Create intermediate pairs: (u, |v1,v2,v3...)
		// "|" is the control char denoting it's links rather than PR
		output.collect(new Text(page_u), new Text("|" + links));
		// "!" is the control char denoting it's a node with the old PR
		output.collect(new Text(page_u), new Text("!" + rank));
    }
}

