package org.myorg;

import java.io.IOException;
import java.util.*;
import java.text.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.Counters;
import org.apache.hadoop.mapred.Counters.Counter;

public class GaussPageRank {    
	private static NumberFormat nf = new DecimalFormat("00");
	public static final int N = 685230;
	
    public static void main(String[] args) throws Exception {
    	GaussPageRank rank = new GaussPageRank();
    	ArrayList<Double> residualArray = new ArrayList<Double> ();
    	ArrayList<Double> residualAndHighestRank = new ArrayList<Double> ();
    
    	int numPass = 0;
    	System.out.println("MapReduce Pass " + numPass + ":");
    	
        residualAndHighestRank = rank.runPageRankCalculation(args[0], args[1] + "/iter" + nf.format(0 + 1));
        double averageResidual  = residualAndHighestRank.get(0);
    	residualArray.add(averageResidual);

    	while (averageResidual > 0.001) {
    		++ numPass;
    		/*-------------------DEBUG--------------------------*/
        	//if (numPass == 6) break;
        	/*-------------------DEBUG--------------------------*/
    		System.out.println("MapReduce Pass " + numPass + ":");
    		residualAndHighestRank = rank.runPageRankCalculation(args[1] + "/iter" + nf.format(numPass), args[1] + "/iter" + nf.format(numPass + 1));
    		averageResidual  = residualAndHighestRank.get(0);
        	residualArray.add(averageResidual);
        }
        
    	System.out.println("Brief Summary:");
    	System.out.println("Total Number of Passes: " + (numPass + 1));
        // Print residuals:
        for (int i = 0; i < residualArray.size(); ++ i) {
        	String residualStr = new String(String.format("Average Residual Error for Pass %d: %.4g", i, residualArray.get(i)));
        	System.out.println(residualStr);
        }
        
        // Print Highest values from the last pass
        for (int i = 1; i < residualAndHighestRank.size(); ++ i) {
        	System.out.println("Highest Rank Node in Block " + (i-1) + " is " + Math.round(residualAndHighestRank.get(i)));
        }
    }
    
    public ArrayList<Double> runPageRankCalculation(String InputPath, String OutputPath) throws Exception {    	
    	JobConf conf = new JobConf(GaussPageRank.class);
        conf.setJarByClass(GaussPageRank.class);
    	 
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);
 
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
 
        FileInputFormat.setInputPaths(conf, new Path(InputPath));
        FileOutputFormat.setOutputPath(conf, new Path(OutputPath));
 
        conf.setMapperClass(BlockedPageRankCalculateMap.class);
        conf.setReducerClass(BlockedPageRankCalculateReduce.class);
 
        RunningJob job = JobClient.runJob(conf);
        
        // Use counter to get the residual
        Counters counters = job.getCounters();
        Counter c = counters.findCounter(MyCounter.GLOBAL_RESIDUAL_COUNTER);
        double residualSum = c.getValue() / (1.0*10e6);
        
        c = counters.findCounter(MyCounter.LOCAL_ITERATION_NUM_COUNTER);
        long iter_sum = c.getValue();
        c = counters.findCounter(MyCounter.BLOCK_COUNTER);
        long block_num = c.getValue();
        
        //System.out.println("Total Iterations in this MapRed pass: " + iter_sum);
        //System.out.println("Total Number of blocks: " + reducer_num);
        long avg_iter_per_block = Math.round(iter_sum * 1.0 / block_num);
  
        System.out.println("Average number of iterations per Block: " + avg_iter_per_block);
        
        ArrayList<Double> residualAndHighestRank = new ArrayList<Double> ();
        
        // residualSum = residualSum / reducer_num;
        double averageResidual = residualSum / GaussPageRank.N;
        String residualStr = new String(String.format("Global Average Residual Error: %.4g", averageResidual));
        residualAndHighestRank.add(averageResidual);
        System.out.println(residualStr);
        
        for (int i = 0; i < block_num; ++ i) {
        	c = counters.findCounter(HighestRankCounter.values()[i]);
        	double highestValue = c.getValue();
        	residualAndHighestRank.add(highestValue);
        	//System.out.println("Highest Rank in Blcok " + i + " is " + highestValue);
        }
        
        return residualAndHighestRank;
    }
    
    
}
