package org.squery

class OneToMany(var self:MColumn,var that:MColumn,var ref:String) extends Relation{
	
}

object OneToMany{
	def apply(s:MColumn,t:MColumn,c:String):OneToMany={
		return new OneToMany(s,t,c);
	}
}