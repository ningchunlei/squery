package org.squery.sql
import org.squery.Column
import org.squery.Table
import org.squery.util.Primitive

object Fun {
	
	def apply(c:Column,fn:String,args:Any*):Column={
		var cp:Column = c.myCloneImpl;
		c.fun  = fn;
		var sb:StringBuilder = new StringBuilder(fn);
		sb.append("(")
		for(x <- args.toList){
			if(Primitive.isPrimitive(x)){
				if(x.isInstanceOf[String]
				   || x.isInstanceOf[Char]){
					sb.append("'").append(x).append("'");
				}else{
					sb.append(x);
				}
				sb.append(",");
			}else{
				var c:Column = x.asInstanceOf[Column];
				var tname:String = c.table.table
			    if(c.tb !=null){
				   tname = c.tb;
			    }	
				var prefix:String = String.format("%s.%s", tname,c.name);
				sb.append(prefix).append(",")
			}    
		}
		if(sb.last==','){
			sb.deleteCharAt(sb.length-1);
		}
		sb.append(")");
		c.qName = sb.toString;
		c.asStr = c.table.table + "_" + fn;
		return c;
	}
	
	def apply(fn:String,args:Any*):Column={
		var c:Column = new Column("",new Table(""));
		c.fun  = fn;
		var sb:StringBuilder = new StringBuilder(fn);
		sb.append("(")
		for(x <- args.toList){
			if(Primitive.isPrimitive(x)){
				if(x.isInstanceOf[String]
				   || x.isInstanceOf[Char]){
					sb.append("'").append(x).append("'");
				}else{
					sb.append(x);
				}
				sb.append(",");
			}else{
				var c:Column = x.asInstanceOf[Column];
				var tname:String = c.table.table
			    if(c.tb !=null){
				   tname = c.tb;
			    }	
				var prefix:String = String.format("%s.%s", tname,c.name);
				sb.append(prefix).append(",")
			}    
		}
		if(sb.last==','){
			sb.deleteCharAt(sb.length-1);
		}
		sb.append(")");
		c.qName = sb.toString;
		c.asStr = "fun";
		return c;
	}
	
}