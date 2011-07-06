package org.squery.sql
import org.squery.sql.dialect.Ansi
import org.squery.Column

class GroupBy(var g:List[Column]) extends BaseClause{
	
	def execute(sql:Ansi){
		sql.groupby(this);
	}
	
}