package org.squery.sql.dialect
import org.squery.sql.Select
import org.squery.Column
import org.squery.Table
import org.squery.sql.BaseClause
import org.squery.op.CBaseOp
import org.squery.op.CEqual
import org.squery.sql.From
import org.squery.sql.Where
import org.squery.sql.InnerJoin
import org.squery.sql.LeftJoin
import org.squery.sql.RightJoin
import org.squery.sql.On
import org.squery.sql.GroupBy
import org.squery.sql.Having
import org.squery.sql.OrderBy
import org.squery.sql.Limit
import org.squery.sql.Insert
import org.squery.op.LiteralV
import org.squery.sql.Update
import org.squery.sql.Delete
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Stack

abstract class Ansi {

	var sql:StringBuilder = new StringBuilder();
	var params:ListBuffer[Any] = new ListBuffer[Any]();
	var treeColumn:ListBuffer[ListBuffer[CBaseOp]] = ListBuffer[ListBuffer[CBaseOp]](); 
	var sColumn:ListBuffer[CBaseOp] = new ListBuffer[CBaseOp]();
	var tTable:ListBuffer[Table] = new ListBuffer[Table]();
	var openWhere:Boolean = false;
	var cacheable:Boolean = true;
	var updating:Boolean = false;

	def select(s:Select){
		var cl:List[Column] = s.cl;
		sql.append("SELECT");
		for(c <- cl){
			var tname:String = c.table.table
			if(c.tb !=null ){
				tname = c.tb;
			}
			var prefix:String = c.qName ;
			var postfix:String = "";
			if(c.asStr != null){
				postfix = String.format(" as %s", c.asStr );
			}
			sql.append(" ").append(prefix).append(postfix).append(",");
		}
		sql.deleteCharAt(sql.length-1);
	}

	def from(s:From)={
		var t:List[Table] = s.tables;
		sql.append(" FROM ");
		for(tb <- t){
			if(tb.clause != null){
				sql.append(" (")
				tb.clause.execute(this);
				sql.append(String.format(" ) as %s",tb.asTb))
				cacheable = false;
			}else if(tb.table!=null){
				sql.append(tb.getPartionTable)
				if(tb.asTb != null){
					sql.append(String.format(" as %s",tb.asTb));
				}
				tTable += tb;
			}
			sql.append(",")
		}
		sql.deleteCharAt(sql.length-1);
	}

	def innerjoin(s:InnerJoin)={
		var t:List[Table] = s.tables;
		sql.append(" INNER JOIN ");
		for(tb <- t){
			if(tb.clause != null){
				sql.append(" (")
				tb.clause.execute(this);
				sql.append(String.format(" ) as %s",tb.asTb))
				cacheable = false;
			}else if(tb.table!=null){
				sql.append(tb.getPartionTable)
				if(tb.asTb != null){
					sql.append(String.format(" as %s",tb.asTb));
				}
				tTable += tb;
			}
			sql.append(",")
		}
		sql.deleteCharAt(sql.length-1);
	}

	def leftjoin(s:LeftJoin)={
		var t:List[Table] = s.tables;
		sql.append(" LEFT JOIN ");
		for(tb <- t){
			if(tb.clause != null){
				sql.append(" (")
				tb.clause.execute(this);
				sql.append(String.format(" ) as %s",tb.asTb))
				cacheable = false;
			}else if(tb.table!=null){
				sql.append(tb.getPartionTable)
				if(tb.asTb != null){
					sql.append(String.format(" as %s",tb.asTb));
				}
				tTable += tb;
			}
			sql.append(",")
		}
		sql.deleteCharAt(sql.length-1);
	}

	def rightjoin(s:RightJoin)={
		var t:List[Table] = s.tables;
		sql.append(" RIGHT JOIN ");
		for(tb <- t){
			if(tb.clause != null){
				sql.append(" (")
				tb.clause.execute(this);
				sql.append(String.format(" ) as %s",tb.asTb))
				cacheable = false;
			}else if(tb.table!=null){
				sql.append(tb.getPartionTable)
				if(tb.asTb != null){
					sql.append(String.format(" as %s",tb.asTb));
				}
				tTable += tb;
			}
			sql.append(",")
		}
		sql.deleteCharAt(sql.length-1);
	}

	def on(s:On)={
		sql.append(" ON ");
		this.openWhere = true;
		s.opt.exe(this);
		this.openWhere = false;
	}

	def where(s:Where)={
		sql.append(" WHERE ");
		this.openWhere = true;
		this.updating = true;
		s.opt.exe(this);
		this.openWhere = false;
		this.updating = false;
	}

	def groupby(g:GroupBy)={
		sql.append(" GROUP BY ");
		for(i <- g.g){
			sql.append(String.format(" %s  %s,", i.qName,i.ord));
		}
		if(sql.last==','){
			sql.deleteCharAt(sql.length-1);
		}
	}

	def orderby(g:OrderBy)={
		sql.append(" ORDER BY ");
		for(i <- g.g){
			sql.append(String.format(" %s  %s,", i.qName,i.ord))
		}
		if(sql.last==','){
			sql.deleteCharAt(sql.length-1);
		}
	}

	def having(s:Having)={
		sql.append(" HAVING ");
		s.opt.exe(this);
		cacheable = false;
	}

	def limit(l:Limit)={
		sql.append(" LIMIT ");
		l.t.foreach(i=> sql.append(i).append(","));
		if(sql.last==','){
			sql.deleteCharAt(sql.length-1);
		}
	}

	def forupdate()={
		sql.append(" FOR UPDATE");
	}

	def insert(i:Insert)={
		sql.append(String.format("INSERT INTO %s ", i.t.getPartionTable));
		if(i.c .size>0){
			sql.append(" (")
			i.c.foreach(c => sql.append(c.name).append(","))
			sql.deleteCharAt(sql.length-1);
			sql.append(")");
		}
		if(i.vs!=null){
			sql.append(" (")
			i.vs.execute(this);
			sql.append(")");
		}else{
			sql.append(" VALUES(");
			var count:Int=0;
			for(t <- i.vv){
				var str:String="";
				if(t.isInstanceOf[LiteralV[_]]){
					val ct:LiteralV[_] = t.asInstanceOf[LiteralV[_]];
					if(ct.v.isInstanceOf[Int]){
						str = String.format(" %d,",ct.v.asInstanceOf[Integer]);
					}else if(ct.v.isInstanceOf[Long]){
						str = String.format(" %ld,",ct.v.asInstanceOf[java.lang.Long]);
					}else if(ct.v.isInstanceOf[Short]){
						str = String.format(" %d,",ct.v.asInstanceOf[java.lang.Short]);
					}else if(ct.v.isInstanceOf[Float]){
						str = String.format(" %f," ,ct.v.asInstanceOf[java.lang.Float]);
					}else if(ct.v.isInstanceOf[Double]){
						str = String.format(" %lf," ,ct.v.asInstanceOf[java.lang.Double]);
					}else if(ct.v.isInstanceOf[String]){
						var c:String = ct.asInstanceOf[String]
						str = String.format(" '%s',",ct.v.asInstanceOf[String]);
					}
				}else{
					str = String.format(" ?,");
					params  += t;
				}
				count += 1;
				sql.append(str);
			}
			if(sql.last==','){
				sql.deleteCharAt(sql.length-1);
			}
			sql.append(" )");
		}
	}

	def update(i:Update)={
		sql.append(String.format("UPDATE  %s ", i.arg.getPartionTable));
		sql.append(" SET ")
		for(s <- i.sets){
			s.op(this);
			sql.append(" ,")
		}
		if(sql.last==','){
			sql.deleteCharAt(sql.length-1);
		}
	}

	def delete(i:Delete)={
		sql.append(String.format("DELETE  FROM %s ", i.arg.getPartionTable));
	}
}