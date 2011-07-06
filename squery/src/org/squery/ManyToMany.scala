package org.squery

class ManyToMany(var self:MColumn,var that:MColumn,var ref:String) extends Relation{
	
}

object ManyToMany{
	def apply(s:MColumn,t:MColumn,c:String):ManyToMany={
		return new ManyToMany(s,t,c);
	}
}