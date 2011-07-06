package org.squery

class MColumn(var m:List[Column]) {
	
}

object MColumn{
	def apply(arg:Column*):MColumn={
		return new MColumn(arg.toList);
	}
}