package org.squery.index
import kafka.serializer.Encoder
import kafka.message.Message

class SelectEncoder extends Encoder[Array[Byte]] {
	
	 def toMessage(obj:Array[Byte]):Message ={
		return new Message(obj);
	 }
	
}