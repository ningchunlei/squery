package org.squery.sql
import org.squery.op.CExist

object Exist{
	
	def apply(args:Select):CExist={
		return new CExist(args);
	}
	
}