package org.squery.sql
import org.squery.Table
import org.squery.Column
import org.squery.op.CBaseOp
import scala.collection.mutable.MutableList
import org.squery.sql.dialect.Ansi

class Update(var arg:Table) extends BaseClause{
	var sets:MutableList[CBaseOp] = new MutableList[CBaseOp];
	def set(v:CBaseOp):Update={
		sets += v;
		return this;
	}
	
	def execute(sql:Ansi)={	
		sql.update(this);
		clist.foreach(_.execute(sql));
		println(sql.sql .toString);
	}
}

object Update {
	
	def apply(arg:Table):Update={
		return new Update(arg);
	}
	
}