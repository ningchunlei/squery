package org.squery.util

object Primitive {
	
	def isPrimitive(x:Any):Boolean = {
		x match {
			case t:Int => return true;
			case t:Long => return true;
			case t:Float => return true;
			case t:Double => return true;
			case t:Boolean => return true;
			case t:Char => return true;
			case t:Byte => return true;
			case t:Short => return true;
			case t:String => return true;
			case _ => 
		}
		return false;
	}
	
	def isPrimitive(x:Class[_]):Boolean={
		if(x==classOf[Int]){
			return true;
		}else if(x==classOf[Long]){
			return true;
		}else if(x==classOf[Float]){
			return true;
		}else if(x==classOf[Double]){
			return true;
		}else if(x==classOf[Boolean]){
			return true;
		}else if(x==classOf[Char]){
			return true;
		}else if(x==classOf[Byte]){
			return true;
		}else if(x==classOf[Short]){
			return true;
		}else if(x==classOf[String]){
			return true;
		}
		return false;
	}
	
	def get(x:Class[_]):Any={
		if(x==classOf[Int]){
			return 0;
		}else if(x==classOf[Long]){
			return 0l;
		}else if(x==classOf[Float]){
			return 0f;
		}else if(x==classOf[Double]){
			return 0d;
		}else if(x==classOf[Boolean]){
			return false;
		}else if(x==classOf[Char]){
			return ' ';
		}else if(x==classOf[Byte]){
			return 1.asInstanceOf[Byte];
		}else if(x==classOf[Short]){
			return 2.asInstanceOf[Short];
		}else if(x==classOf[String]){
			return "";
		}
		return null;
	}
	
}