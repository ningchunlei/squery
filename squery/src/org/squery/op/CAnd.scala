package org.squery.op
import org.squery.sql.dialect.Ansi

case class CAnd(var left:CBaseOp,var right:CBaseOp) extends CBaseOp{
	
	def op(sql:Ansi)={
		sql.sql.append(" and ");
		if(right!=null){
			right match{
				case CAnd(_,_) => sql.sql.append(" (");
				case COr(_,_) => sql.sql.append(" (");
				case _ =>;
			}
		}
	}
	
	override def opAfter(sql:Ansi)={
		if(right!=null){
			right match{
				case CAnd(_,_) => sql.sql.append(" )");
				case COr(_,_) => sql.sql.append(" )");
				case _ =>;
			}
		}
	}

}
