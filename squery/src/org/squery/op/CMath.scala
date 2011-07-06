package org.squery.op
import org.squery.Column
import org.squery.sql.dialect.Ansi

class CMath(c:Column,v:Int,math:String) extends CBaseOp{
	
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi) = {
		sql.sql.append(String.format(" %s%s%d",c.qName,math,v.asInstanceOf[java.lang.Integer]));
	}
	
}