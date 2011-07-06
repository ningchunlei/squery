package org.squery.sql
import org.squery.op.CBaseOp
import org.squery.sql.dialect.Ansi

class Having (var opt:CBaseOp) extends BaseClause{
	
	def execute(sql:Ansi){
		sql.having(this);
	}
	
}