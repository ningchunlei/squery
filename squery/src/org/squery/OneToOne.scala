package org.squery

class OneToOne(var self:MColumn,var that:MColumn,var ref:String) extends Relation{
	
}

object OneToOne{
	def apply(s:MColumn,t:MColumn,c:String):OneToOne={
		return new OneToOne(s,t,c);
	}
}

