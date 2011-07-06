package org.squery.custom;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.squery.index.SelectData.SelectIndex;
import org.squery.index.SelectData.SelectIndex.SQLType;


import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaMessageStream;
import kafka.javaapi.consumer.ConsumerConnector;

public class Custom {
	
	static ExecutorService executor = Executors.newFixedThreadPool(10);
	static AtomicLong sem = new AtomicLong(0);
	public static void run(){
		// specify some consumer properties
		Properties props = new Properties();
		props.put("zk.connect", "192.168.10.84:2181");
		props.put("zk.connectiontimeout.ms", "1000000");
		props.put("groupid", "custom");
		// Create the connection to the cluster
		ConsumerConfig consumerConfig = new ConsumerConfig(props);
		ConsumerConnector consumerConnector = Consumer.createJavaConsumerConnector(consumerConfig);
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		map.put("sql", 1);
		// create 4 partitions of the stream for topic “test”, to allow 4 threads to consume
		Map<String, List<KafkaMessageStream>> topicMessageStreams = 
		    consumerConnector.createMessageStreams(map);
		while(true){
			KafkaMessageStream streams = topicMessageStreams.get("sql").get(0);
			ConsumerIterator it = streams.iterator();
			while(it.hasNext()){
				try{
					ByteBuffer buffer = it.next().payload();
				    byte [] bytes = new byte[buffer.remaining()];
				    buffer.get(bytes);
					final SelectIndex sql = SelectIndex.parseFrom(bytes);
				    if(sql.getSqlType()==SQLType.SELECT){
				    	   executor.submit(new Runnable(){
				    		  public void run(){
				    			  try{
				    				  sem.incrementAndGet();
				    				  CustomSql.select(sql.getSlsql());
				    			  }catch(Exception e){
				    				  e.printStackTrace();
				    			  }finally{
				    				  sem.decrementAndGet();
				    			  }
				    		  }
				    	   });
			       }else{
			    	   while(true){
			    		   try {
			    			   Thread.sleep(200);
			    		   }catch (InterruptedException e) {
								e.printStackTrace();
						   }
			    		   if(sem.get()==0){
			    			   break;
			    		   }
			    	   }
			    	   CustomSql.update(sql);
			       }
				}catch(Exception e){
					e.printStackTrace();
				}
			 }	
			
		}
		
	}
	
	public static void main(String[] args){
		Custom.run();
		
	}
}
