package org.squery
import java.lang.reflect.Field
import scala.collection.mutable.ListBuffer

class RTree {
	
	var list:ListBuffer[RNode] = new ListBuffer[RNode]();

	def find(t:Table):RNode={
		for(i <- list){
			var buf:ListBuffer[RNode] = ListBuffer[RNode]();
			val c:RNode = search(t,i,buf);
			if(c!=null){
				return c;
			}
		}
		return null;
	}
	
	def insert(l:Column,r:Column)={
	
		var lr:RNode = find(l.table);
	
		var rr:RNode = find(r.table);
	
		var f:Boolean = false;
		if(lr==null && rr==null){
			lr = new RNode();
			lr.table = l.table;
			
			rr = new RNode();
			rr.table = r.table;
			
			f = true;
		}else if(lr==null){
			lr = new RNode();
			lr.table = l.table;
		}else if(rr==null){
			rr = new RNode();
			rr.table = r.table;
		}
		var ff:Boolean = insertR(lr,l,rr,r);
		if(ff && f){
			list += lr;
		}
	}
	
	
	def removeNotWhole()={
		
		for(f <- list){
			var buf:ListBuffer[RNode] = ListBuffer[RNode]();
			removeSearch(f,buf);
		}
		
	}
	
	def insertR(lr:RNode,l:Column,rr:RNode,r:Column):Boolean={
		var f:Boolean = false;
		if(l.table._relation!=null){
			for(m<-l.table._relation;if m.self.m.find(i => i.name == l.name && i.table.table == l.table .table ).size>0
			&& m.that.m.find(i => i.name == r.name && i.table.table == r.table.table).size>0){
				
				lr.insert(rr, m, l, r);
			
				f = true;
			}
		}
		
	   if(r.table._relation!=null){
			for(m<-r.table._relation;if m.self.m.find(i => i.name == r.name && i.table.table==r.table.table).size>0
			&& m.that.m.find(i => i.name == l.name && i.table.table == l.table.table).size>0){
			
				rr.insert(lr, m, r, l);
				
				f = true;
			}
	   }
	    return f;
	}
	
	private def removeSearch(r:RNode,buf:ListBuffer[RNode]):Unit={
		if(buf.find(_==r).size>0){
			return;
		}else{
			buf += r;
		}
		for(i <- r.rm ){
			var c:ListBuffer[RColumn] = i._2.filter(f => !(f.r.self.m.size==f.self.size && 
						f.r.that.m.size==f.that.size));
			if(c.size>0){
				r.rm.update(i._1 , c);
			}
		}
		for(i <- r.rt){
			removeSearch(i,buf);
		}
	}
	
	private def search(t:Table,r:RNode,buf:ListBuffer[RNode]):RNode={
		if(buf.find(_==r).size>0){
			return null;
		}else{
			buf += r;
		}
		if(r.table.table==t.table){
			return r;
		}	
		
		for(i <- r.rt){
			var c:RNode = search(t,i,buf);
			if(c!=null){
				return c;
			}
		}
		
		return null;
	}
	
}