package org.squery.op

class LiteralV[T](var v:Any){
	
}

object LiteralV {
	
	def apply(s:Any):LiteralV[Any]={
		return new LiteralV[Any](s);
	}
	
}