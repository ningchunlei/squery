package org.squery.op
import org.squery.sql.Select
import org.squery.sql.dialect.Ansi

case class CNotExist(var s:Select) extends CBaseOp{
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi)={
		sql.sql.append(" NOT EXIST (")
		s.execute(sql);
		sql.sql .append(") "); 
	}
	
}