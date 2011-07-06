package org.squery.sql
import org.squery.Column
import org.squery.op.CBaseOp
import org.squery.Table
import org.squery.sql.dialect.Ansi

class Select(var cl:List[Column]) extends BaseClause{
	
	def from(arg:Table*):Select={
		clist.+=(new From(arg.toList));
		return this;
	}
	
	def innerjoin(arg:Table*):Select={
		clist.+=(new InnerJoin(arg.toList));
		return this;
	}
	
	def leftjoin(arg:Table*):Select={
		clist.+=(new LeftJoin(arg.toList));
		return this;
	}
	
	def rightjoin(arg:Table*):Select={
		clist.+=(new RightJoin(arg.toList));
		return this;
	}
	
	def on(x:CBaseOp):Select = {
		clist +=(new On(x))
		return this;
	}
	
	def execute(sql:Ansi)={	
		sql.select(this);
		clist.foreach(_.execute(sql));
		println(sql.sql.toString);
	}
	
}

object Select {
	
	var single:Int = 0;
	var set:Int = 1;
	
	def apply(args:Column*):Select={
			return new Select(args.toList);
	}
	
	def apply(args:List[Column]):Select={
			return new Select(args);
	}
}