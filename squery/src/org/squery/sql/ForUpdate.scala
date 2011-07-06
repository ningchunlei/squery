package org.squery.sql
import org.squery.sql.dialect.Ansi

class ForUpdate extends BaseClause{
	
	def execute(sql:Ansi){
		sql.forupdate;
	}
}