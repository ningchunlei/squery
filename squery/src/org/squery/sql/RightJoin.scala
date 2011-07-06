package org.squery.sql
import org.squery.Table
import org.squery.sql.dialect.Ansi

class RightJoin (val tables:List[Table]) extends BaseClause{
	
	def execute(sql:Ansi)={
		sql.rightjoin(this);
	}
	
}