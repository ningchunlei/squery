package org.squery.op
import org.squery.sql.dialect.Ansi
import org.squery.Column
import org.squery.sql.Select

case class CIn(var c:Column,var t:Any) extends CBaseOp{
	
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi)={
		var sb:StringBuilder = sql.sql ;
		sb.append(String.format(" %s in (",c.qName ))
		if(t.isInstanceOf[List[LiteralV[_]]]){
			var ls:List[LiteralV[_]] = t.asInstanceOf[List[LiteralV[_]]];
			for(i <- ls){
				if(i.v.isInstanceOf[String]){
					sb.append(String.format("'%s',",i.v.asInstanceOf[String]));
				}else{
					sb.append(i.v).append(",");
				}
			}
			sb.deleteCharAt(sb.length-1);
			
		}else if(t.isInstanceOf[List[_]]){
			var ls:List[Any] = t.asInstanceOf[List[Any]];
			ls.foreach(i => sb.append("?,"));
			sb.deleteCharAt(sb.length-1);
			sql.params += (ls);
		}else if(t.isInstanceOf[Select]){
			var st:Select = t.asInstanceOf[Select];
			st.execute(sql);
			sql.cacheable =false;
		}
		sb.append(")");
	}
	
}