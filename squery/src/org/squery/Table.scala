package org.squery
import org.squery.sql.BaseClause
import scala.collection.mutable.HashMap

class Table(var table:String) extends Cloneable{
	implicit def _table = this;
	implicit def convertToColumn(title:String) = Column(title);

	var _relation:List[Relation] = null;
	var _pk:MColumn = null;
	var _clazz:Class[_] = null;
	var _updating:List[Column] = null;
	var _selecting:List[Column] = null;
	var asTb:String = null;
	var clause:BaseClause = null;
	var m_partion:HashMap[Column,Any] = null;

	def this(t:String,c:BaseClause)={
		this(t);
		asTb = t;
		clause = c;
	}

	def as(t:String):Table = {
		var tb:Table = myCloneImpl();
		tb.asTb  = t;
		return tb;
	}

	def partion(t:HashMap[Column,Any]):Table={
		var tb:Table = myCloneImpl();
		tb.m_partion = t;
		return tb;
	}

	def getPartionTable:String={
		return table;
	}

	def myCloneImpl():Table = {
		val justLikeMe = this.clone
		justLikeMe.asInstanceOf[Table];
	}
}