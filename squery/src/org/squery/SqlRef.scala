package org.squery
import java.lang.reflect.Field
import scala.collection.mutable.HashMap

object SqlRef {
	
	var m:HashMap[String,String] = new HashMap[String,String]();
	var mtype:HashMap[String,Field] = new HashMap[String,Field]();
	
	def get(c:Column):String={
		var res:String = m.get(String.format("%s.%s", c.table.table,c.name)).getOrElse(null);
		if(res!=null){
			return res;
		}
		var fs:List[Field] = c.table.getClass.getDeclaredFields().toList;
		for(f <- fs){
			f.setAccessible(true);
			if(f.getType==classOf[Column]){
				
				var id:Column = f.get(c.table).asInstanceOf[Column];
				if(id.table.table == c.table.table 
						&& id.name == c.name ){
					m.put(String.format("%s.%s", c.table.table,c.name), f.getName);
					return f.getName;
				}
			}
			
		}
		return null;
	}
	
	def getType(f:String,o:AnyRef):Class[_]={
		var k:String = f +o.getClass;
		var t:Field = mtype.get(k).getOrElse(null);
		if(t!=null){
			return t.getType;
		}
		var fie:Field = o.getClass.getDeclaredField(f); 
		fie.setAccessible(true);
		mtype.put(k, fie)
		return fie.getType;
	}
		
	def set(f:String,o:AnyRef,v:Any){
		var k:String = f +o.getClass;
		var t:Field = mtype.get(k).getOrElse(null);
		if(t!=null){
			t.set(o, v);
		}
	}
	
	def get(f:String,o:AnyRef):Any={
		var k:String = f +o.getClass;
		var t:Field = mtype.get(k).getOrElse(null);
		if(t==null){
			getType(f,o);
			t = mtype.get(k).getOrElse(null);
		}
		return t.get(o);
	}
	
}