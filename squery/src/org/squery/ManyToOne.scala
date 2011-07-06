package org.squery

class ManyToOne(var self:MColumn,var that:MColumn,var ref:String) extends Relation{
	
}

object ManyToOne{
	def apply(s:MColumn,t:MColumn,c:String):ManyToOne={
		return new ManyToOne(s,t,c);
	}
}