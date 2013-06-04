import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;
import java.util.Scanner;


public class preprocess {
	
	public static void main(String []args)throws IOException{
		SingleNodePreprocess("./data/edges.txt","./data/singlenode_edge");
		BlockNodePreprocess("./data/edges.txt","./data/nodes.txt","./data/block_edge");
		generateRandomBlocks("./data/random_node");
		BlockNodePreprocess("./data/edges.txt","./data/random_node","./data/random_block_edge");
	}
	 /*
	  * input: edge.txt
	  * output: 
	  */
	 public static void SingleNodePreprocess(String input,String output)throws IOException{
		 Scanner s = new Scanner(new File(input));
		 Hashtable<Integer, ArrayList<Integer>> graph = new Hashtable<Integer, ArrayList<Integer>>();
		 int max = 0;
		 while(s.hasNext()){
			 String line = s.nextLine();
			 String [] seg = line.split(" ");
			 int i = 0;
			 Integer from = -1;
			 for(;;i++){
				 if(seg[i].length()>0){
					 from = Integer.parseInt(seg[i]);
					 i++;
					 break;
				 }
			 }
			 max = from;
			 Integer to = -1;
			 for(;;i++){
				 if(seg[i].length()>0){
					 to = Integer.parseInt(seg[i]);
					 i++;
					 break;
				 }
			 }
			 Double d = 0.0;
			 for(;;i++){
				 if(seg[i].length()>0){
					 d = Double.parseDouble(seg[i]);
					 break;
				 }
			 }
			 //apply filter here
			 if(d>=0.99*0.35&&d<0.99*0.35+0.01){
				 continue;
			 }
			 ArrayList<Integer> conn = new ArrayList<Integer> ();
			 if(graph.containsKey(from)){
				 conn = graph.get(from);
			 }
			 conn.add(to);
			 graph.put(from, conn);
		 }
		 s.close();//done with the input
		 BufferedWriter out = new BufferedWriter(new FileWriter(output));
		 for(int i = 0;i<=max;i++){
			 ArrayList<Integer> temp = graph.get(i);
			 String sout = "";
			 if(temp!=null){
				 for(Integer t:temp){
					 sout = sout+t+",";
				 }
				 sout = sout.substring(0,sout.length()-1);
			 }
			 out.write(i+"\t"+1.0+"\t"+sout+"\n");
		 }
		 out.close();
	 }
	 
	 public static void BlockNodePreprocess(String edge, String node, String output)throws IOException{
		 Scanner s = new Scanner(new File(node));
		 Hashtable<Integer, Integer> table = new Hashtable<Integer, Integer> ();
		 Hashtable<Integer, ArrayList<Integer>> edgeinfo = new Hashtable<Integer, ArrayList<Integer>> ();
		 while(s.hasNext()){
			 String line = s.nextLine();
			 String [] data = line.split(" ");
			 Integer n = -1;
			 int i = 0;
			 for(;;i++){
				 if(data[i].length()>0){
					 n = Integer.parseInt(data[i]);
					 i++;
					 break;
				 }
			 }
			 Integer b = -1;
			 for(;;i++){
				 if(data[i].length()>0){
					 b = Integer.parseInt(data[i]);
					 break;
				 }
			 }
			 table.put(n, b);//put node id and block id into the table
		 }
		 s.close();
		 s= new Scanner(new File(edge));
		 while(s.hasNext()){
			 String line = s.nextLine();
			 String [] data = line.split(" ");
			 int i = 0;
			 Integer from = -1;
			 Integer to = -1;
			 Double d = 0.0;
			 for(;;i++){
				 if(data[i].length()>0){
					 from = Integer.parseInt(data[i]);
					 i++;
					 break;
				 }
			 }
			 for(;;i++){
				 if(data[i].length()>0){
					 to = Integer.parseInt(data[i]);
					 i++;
					 break;
				 }
			 }
			 for(;;i++){
				 if(data[i].length()>0){
					 d = Double.parseDouble(data[i]);
					 break;
				 }
			 }
			 if(d>=0.99*0.35&&d<0.99*0.35+0.01){
				 continue;
			 }
			 ArrayList<Integer> temp = new ArrayList<Integer>();
			 if(edgeinfo.containsKey(from)){
				 temp = edgeinfo.get(from);
			 }
			 temp.add(to);
			 edgeinfo.put(from, temp);
		 }
		 s.close();
		 BufferedWriter out = new BufferedWriter(new FileWriter(output));
		 for(int i = 0;i<table.size();i++){
			 ArrayList<Integer> temp = edgeinfo.get(i);
			 int block = table.get(i);
			 String sout = i+"+"+block+"\t"+1.0+"\t";
			 if(temp!=null){
				 for(Integer t:temp){
					 int b = table.get(t);
					 sout = sout+t+"+"+b+",";
				 }
				 sout = sout.substring(0,sout.length()-1);
			 }
			 out.write(sout+"\n");
		 }
		 out.close();
	 }
	 
	 public static int hash(int id){
		 Random r = new Random();
		 return (id<<1)|(id+r.nextInt());
	 }
	 
	 public static void generateRandomBlocks(String output)throws IOException{
		 BufferedWriter out = new BufferedWriter(new FileWriter(output));
		 for(int i = 0;i<=685229;i++){
			 int b = Math.abs(hash(i))%68;
			 out.write(i+"   "+b+"\n");
		 }
		 out.close();
	 }
}