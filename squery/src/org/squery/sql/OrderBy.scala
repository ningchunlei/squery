package org.squery.sql
import org.squery.Column
import org.squery.sql.dialect.Ansi

class OrderBy(var g:List[Column]) extends BaseClause{
	
	def execute(sql:Ansi){
		sql.orderby(this);
	}
	
}