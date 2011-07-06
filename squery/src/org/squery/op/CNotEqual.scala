package org.squery.op
import org.squery.Column
import org.squery.sql.dialect.Ansi

case class CNotEqual (var c:Column,t:Any) extends CBaseOp{
	
	var left:CBaseOp = null;
	var right:CBaseOp = null;
	
	def op(sql:Ansi) = {
		var str:String = null;
		if(t.isInstanceOf[Column]){
			val ct:Column = t.asInstanceOf[Column];
			str = String.format(" %s!=%s",c.qName,ct.qName);
		}else if(t.isInstanceOf[LiteralV[_]]){
			val ct:LiteralV[_] = t.asInstanceOf[LiteralV[_]];
			if(ct.v.isInstanceOf[Int]){
				str = String.format(" %s!=%d",c.qName ,ct.v.asInstanceOf[Integer]);
			}else if(ct.v.isInstanceOf[Long]){
				str = String.format(" %s!=%ld",c.qName,ct.v.asInstanceOf[java.lang.Long]);
			}else if(ct.v.isInstanceOf[Short]){
				str = String.format(" %s!=%d",c.qName ,ct.v.asInstanceOf[java.lang.Short]);
			}else if(ct.v.isInstanceOf[Float]){
				str = String.format(" %s!=%f",c.qName ,ct.v.asInstanceOf[java.lang.Float]);
			}else if(ct.v.isInstanceOf[Double]){
				str = String.format(" %s!=%lf",c.qName ,ct.v.asInstanceOf[java.lang.Double]);
			}else if(ct.v.isInstanceOf[String]){
				str = String.format(" %s!='%s'",c.qName ,ct.v.asInstanceOf[String]);
			}
		}else{
			str = String.format(" %s!=?",c.qName);
			sql.params  += t;
		}
		
		sql.sql.append(str);
	}
	
	
}
