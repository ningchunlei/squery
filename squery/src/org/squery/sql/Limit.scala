package org.squery.sql
import org.squery.sql.dialect.Ansi

class Limit(var t:List[Int]) extends BaseClause{
	
	def execute(sql:Ansi){
		sql.limit(this);
	}
}