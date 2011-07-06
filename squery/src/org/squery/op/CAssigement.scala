package org.squery.op
import org.squery.Column
import org.squery.sql.dialect.Ansi

class CAssigement(e:Column) extends CBaseOp{
	
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi) = {
		var str:String = null;
		str = String.format(" %s=?",e.qName);
		sql.params  += e;
		sql.sql.append(str);
	}
	
}