package org.squery
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

class RColumn {
	
	var r:Relation = null;
	var self:ListBuffer[Column] = new ListBuffer[Column]();
	var that:ListBuffer[Column] = new ListBuffer[Column]();
}