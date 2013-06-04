package org.myorg;

import java.io.IOException;
import java.util.Iterator;
import java.lang.Math;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class PageRankCalculateReduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
	
	private static final double damping = 0.85F;
    
	public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
    	String currentValue;
    	String links = "";
    	double sumPageRank = 0;
    	double oldPageRank = 0;
    	
        while (values.hasNext()) {
        	currentValue = values.next().toString();
        	//  "|" denote links
        	if (currentValue.startsWith("|")) {
        		links = "\t" + currentValue.substring(1);
        		continue;
        	}
        	// "!" denote node with old page rank
        	if (currentValue.startsWith("!")) {
        		oldPageRank = Double.parseDouble(currentValue.substring(1));
        		continue;
        	}
        	// Calculate new PR.
        	double pageRank = Double.parseDouble(currentValue);
        	sumPageRank += pageRank;
        }
        double newPageRank = damping *  sumPageRank + (1 - damping);
        //double newPageRank = sumPageRank;
       	double residualValue = Math.abs(newPageRank - oldPageRank) / newPageRank;
        //System.out.println("Residual: " + residualValue);
        long amplifiedValue = (long)Math.floor(residualValue * 10e4);
        reporter.incrCounter(MyCounter.RESIDUAL_COUNTER, amplifiedValue);
        output.collect(key, new Text(newPageRank + links));
    }
}
