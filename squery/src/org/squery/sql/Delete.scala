package org.squery.sql
import org.squery.Table
import org.squery.sql.dialect.Ansi

class Delete(var arg:Table) extends BaseClause{
	
	def execute(sql:Ansi)={	
		sql.delete(this);
		clist.foreach(_.execute(sql));
		println(sql.sql .toString);
	}
	
}

object Delete {
	
	def apply(arg:Table):Delete={
		return new Delete(arg);
	}
	
}