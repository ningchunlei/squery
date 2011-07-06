package org.squery.sql
import org.squery.Table
import org.squery.Column
import org.squery.sql.dialect.Ansi

class Insert(var t:Table)(var c:List[Column]) extends BaseClause{
	var vv:List[Any] = null;
	var vs:Select = null
	
	def values(vv:Any*):Insert={
		if(vv.toList.size==1
				&& vv.toList.apply(0).isInstanceOf[Select]){
			this.vs = vv.toList.apply(0).asInstanceOf[Select];
		}else{
			this.vv = vv.toList;
		}
		return this;
	}
	
	def execute(sql:Ansi)={	
		sql.insert(this);
		println(sql.sql .toString);
	}
}

object Insert {
	
	def apply(t:Table)(c:Column*):Insert={
			return new Insert(t)(c.toList);
	}
	
}