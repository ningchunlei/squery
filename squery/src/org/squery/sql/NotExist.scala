package org.squery.sql
import org.squery.op.CNotExist

object NotExist {
	
	def apply(args:Select):CNotExist={
		return new CNotExist(args);
	}
	
}