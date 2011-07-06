package org.squery.util
import java.sql.ResultSet

abstract class Mapping {
	
	def mapping(rs:ResultSet):Any;
	
}