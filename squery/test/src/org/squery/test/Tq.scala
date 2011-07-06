package org.squery.test
import java.util.Properties
import kafka.producer.Producer
import kafka.producer.ProducerConfig
import java.io.ByteArrayOutputStream
import kafka.producer.ProducerData
import scala.collection.mutable.ListBuffer

object Tq {
	
	def main(args: Array[String]) {
		var props:Properties  = new Properties();
		props.put("serializer.class", "org.squery.index.SelectEncoder");
		props.put("zk.connect", "192.168.10.84:2181");
		var config:ProducerConfig = new ProducerConfig(props);
		var producer:Producer[String,Array[Byte]]  = new Producer[String,Array[Byte]](config);
		var o:ByteArrayOutputStream = new  ByteArrayOutputStream();
		o.write(1);
		var l:ListBuffer[Array[Byte]] = new ListBuffer[Array[Byte]]()
		l += "hell".getBytes;
		producer.send(new ProducerData[String,Array[Byte]]("select","aaa",l));
		producer.close();
	}
	
}