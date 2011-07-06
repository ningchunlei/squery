package org.squery.op
import org.squery.sql.dialect.Ansi
import scala.collection.mutable.ListBuffer

abstract class CBaseOp {
	
	var left:CBaseOp;
	var right:CBaseOp;
	var skip:Int = -1;
	def op(sql:Ansi);
	def opAfter(sql:Ansi){
		
	}
	
	def exe(sql:Ansi):Unit={
		if(this.isInstanceOf[COr]){
			sql.treeColumn += new ListBuffer[CBaseOp]();
			left.skip = sql.treeColumn.size-1;
			sql.treeColumn += new ListBuffer[CBaseOp]();
			right.skip = sql.treeColumn.size-1;
		}
		if(this.isInstanceOf[CAnd] && skip != -1){
			left.skip = skip;
			right.skip = skip;
		}
		if(left!=null){
			left.exe(sql);
		}
		op(sql);
		this match{
			case x:CEqual => 
				addList(sql,x);
			case x:CGe =>
				addList(sql,x);
			case x:CGt =>
				addList(sql,x);
			case x:CIn =>
				addList(sql,x);
			case x:CLe =>
				addList(sql,x);
			case x:CLt =>
				addList(sql,x);
			case x:CLike => sql.cacheable=false;
			case x:CNotEqual => sql.cacheable=false;
			case x:CExist => sql.cacheable=false;
			case x:CNotExist => sql.cacheable=false;
			case x:CNotIn => sql.cacheable=false;
			case _ =>  
		}
		if(right!=null){
			right.exe(sql);
		}
		opAfter(sql);
	}
	
	private def addList(sql:Ansi,x:CBaseOp){
		if(sql.openWhere){
			sql.sColumn += this;
			if(sql.updating){
				if(sql.treeColumn.size==0){
					sql.treeColumn += new ListBuffer[CBaseOp]();
				}
				if(this.skip == -1){
					for(k <- sql.treeColumn){
						k += x;
					}
				}else{
					sql.treeColumn.apply(skip) += x; 
				}
			}
		}
	}
	
	def and(t:CBaseOp):CAnd={
		return new CAnd(this,t);
	}
	
	def or(t:CBaseOp):COr={
		return new COr(this,t);
	}
	
}
