package org.squery
import org.squery.op.CEqual
import org.squery.op.CGe
import org.squery.op.CGt
import org.squery.op.CLe
import org.squery.op.CLt
import org.squery.op.CIn
import org.squery.sql.Select
import org.squery.op.CNotIn
import org.squery.op.CLike
import org.squery.op.LiteralV
import org.squery.op.CAssigement
import org.squery.op.CMath

class Column(var name:String,var table:Table) extends Cloneable{
	
	var tb:String=table.table;
	var asStr:String = null;
	var qName:String = String.format("%s.%s", table.table,name);
	var fun:String=null;
	var ord:String = "asc"
	
	def ===(t:Any):CEqual={
		return new CEqual(this,t);
	}
	
	def ====(t:Any):CAssigement={
		return new CAssigement(this);
	}
	
	def +(t:Int):CMath={
		return new CMath(this,t,"+");
	}
	
	def -(t:Int):CMath={
		return new CMath(this,t,"-");
	}
	
	def *(t:Int):CMath={
		return new CMath(this,t,"*");
	}
	
	def /(t:Int):CMath={
		return new CMath(this,t,"/");
	}
	
	def >=(t:Any):CGe={
		return new CGe(this,t);
	}
	
	def >(t:Any):CGt={
		return new CGt(this,t);
	}
	
	def <=(t:Any):CLe={
		return new CLe(this,t);
	}
	
	def <(t:Any):CLt={
		return new CLt(this,t);
	}
	
	def in(t:List[_]):CIn={
		return new CIn(this,t);
	}
	
	def in(t:Select):CIn={
		return new CIn(this,t);
	}
	
	def notin(t:List[_]):CNotIn={
		return new CNotIn(this,t);
	}
	
	def notin(t:Select):CNotIn={
		return new CNotIn(this,t);
	}
	
	def like(t:String):CLike={
		return new CLike(this,t);
	}
	
	def like(t:LiteralV[String]):CLike={
		return new CLike(this,t);
	}
	
	def as(t:String):Column={
		var cl:Column = myCloneImpl();
		cl.asStr = t;
		cl.qName = t;
		return cl;
	}
	
	def asc():Column={
		return this;
	}
	
	def desc():Column={
		var cl:Column = myCloneImpl();
		cl.ord = "desc";
		return cl;
	}
	
	def copy(s:String):Column = {
		var cl:Column = myCloneImpl();
		cl.fun  = null;
		cl.asStr = null;
		cl.name = s;
		cl.qName =  String.format("%s.%s", this.tb,s);
		return cl;
	}
	
	def myCloneImpl():Column = {
		val justLikeMe = this.clone
		justLikeMe.asInstanceOf[Column];
	}
}

object Column {	
	def apply(s:String)(implicit table:Table):Column={
		return new Column(s,table);
	}
}