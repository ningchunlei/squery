package org.squery.op
import org.squery.Column
import org.squery.sql.dialect.Ansi

case class CLike (var c:Column,t:Any) extends CBaseOp{
	
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi) = {
		if(t.isInstanceOf[LiteralV[String]]){
			val ct:LiteralV[String] = t.asInstanceOf[LiteralV[String]];
			sql.sql.append(String.format(" %s like '%s' ",c.qName ,ct.v .asInstanceOf[String]));
		}else {
			sql.sql.append(String.format(" %s like ? ",c.qName));
			sql.params += (t);
		}
	}
	
	
}
