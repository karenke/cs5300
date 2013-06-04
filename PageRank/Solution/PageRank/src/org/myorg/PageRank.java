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

public class PageRank {    
	private static NumberFormat nf = new DecimalFormat("00");
	
	public static final int N = 685230;
	
    public static void main(String[] args) throws Exception {
        PageRank rank = new PageRank();
        double[] residualArray = new double[5];
        
        double residual = rank.runPageRankCalculation(args[0], args[1] + "/iter" + nf.format(0 + 1));
    	residualArray[0] = residual;
        
        for (int numPass = 1; numPass < 5; ++ numPass) {
        	residual = rank.runPageRankCalculation(args[1] + "/iter" + nf.format(numPass), args[1] + "/iter" + nf.format(numPass + 1));
        	residualArray[numPass] = residual;
        }
        
        // Print residuals:
        for (int numPass = 0; numPass < 5; ++ numPass) {
        	System.out.println("Average Residual Value: " + residualArray[numPass] / N);
        }
    }
    
    public double runPageRankCalculation(String InputPath, String OutputPath) throws Exception {    	
    	JobConf conf = new JobConf(PageRank.class);
        conf.setJarByClass(PageRank.class);
    	 
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);
 
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);
 
        FileInputFormat.setInputPaths(conf, new Path(InputPath));
        FileOutputFormat.setOutputPath(conf, new Path(OutputPath));
 
        conf.setMapperClass(PageRankCalculateMap.class);
        conf.setReducerClass(PageRankCalculateReduce.class);
 
        RunningJob job = JobClient.runJob(conf);
        
        // Use counter to get the residual
        Counters counters = job.getCounters();
        Counter c = counters.findCounter(MyCounter.RESIDUAL_COUNTER);
        double residualSum = c.getValue() / (1.0*10e4);
        
        return residualSum;
    }
    
}
