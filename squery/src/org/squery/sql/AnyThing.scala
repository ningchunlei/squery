package org.squery.sql
import org.squery.sql.dialect.Ansi

class AnyThing(var c:String) extends BaseClause{
	
	def execute(sql:Ansi)={
		sql.sql.append(c);
	}
	
}