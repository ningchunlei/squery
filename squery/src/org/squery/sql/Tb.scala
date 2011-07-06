package org.squery.sql
import org.squery.Column

object Tb {
	
	def apply(tb:String,c:Column):Column={
		val cl:Column = c.myCloneImpl();
		cl.tb = tb;
		cl.qName = String.format("%s.%s",tb,cl.name);
		return cl;
	}
	
	def apply(tb:String,c:Column,cstr:String):Column={
		val cl:Column = c.myCloneImpl();
		cl.tb = tb;
		cl.qName = String.format("%s.%s",tb,cstr);
		return cl;
	}
	
	
}