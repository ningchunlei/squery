package org.squery
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer

class RNode {
	
	var rt:ListBuffer[RNode] = new ListBuffer[RNode]();
	var table:Table = null;
	
	var rm:HashMap[String,ListBuffer[RColumn]] = new HashMap[String,ListBuffer[RColumn]]();
	var data:HashMap[String,AnyRef] = new HashMap[String,AnyRef]();
	
	def insert(t:RNode,r:Relation,self:Column,that:Column)={
		if(rt.find(i => i.table.table==t.table.table).size==0){
			rt += t;
		}
		var mlist:ListBuffer[RColumn] = null;
		var list:Option[ListBuffer[RColumn]] = rm.get(t.table.table);
		if(list==None){
			mlist = new ListBuffer[RColumn]();
			rm.put(t.table.table,mlist);
		}else{
			mlist = list.get;
		}
		
		var fd:RColumn = null;
		breakable{for(i <- mlist){
			if(i.r == r){
				fd = i;
				break;
			}
		}
		}
		if(fd==null){
			fd = new RColumn();
			fd.r  = r;
			mlist += fd;
		}
		if(fd.self.find(i => i.name == self.name ).size==0)
			fd.self += self;
		if(fd.that.find(i => i.name == that.name ).size==0)
			fd.that += self;
		
	}
	
	def search(m:String):AnyRef={
		return data.get(m).getOrElse(null);
	}
	
	def put(m:String,o:AnyRef)={
		data.put(m, o);
	}
	
	def search(t:Table):RNode={
		if(t.table==table.table){
			return this;
		}
		for(f <- rt){
			var r:RNode = f.search(t);
			if(r!=null){
				return r;
			}
		}
		return null;
	}
	
	def searchParent(t:Table,p:RNode):RNode={
		if(t.table==table.table){
			if(p==null){
				return this;
			}
			return p;
		}
		for(f <- rt){
			var r:RNode = f.searchParent(t,this);
			if(r!=null){
				return r;
			}
		}
		return null;
	}
}

