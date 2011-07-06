package org.squery.index
import org.squery.sql.Select
import org.squery.index.SelectData.SelectIndex
import org.squery.Column
import org.squery.op.CBaseOp
import org.squery.sql.dialect.Ansi
import org.squery.index.SelectData.SelectIndex._
import org.squery.index.SelectData.SelectIndex.Data
import java.util.Date
import org.squery.index.SelectData.SelectIndex.DataType
import org.squery.op._
import java.util.Properties
import kafka.producer.ProducerConfig
import kafka.producer.Producer
import kafka.producer.ProducerData
import java.io.StringWriter
import java.io.ByteArrayOutputStream
import org.squery.Page
import scala.collection.mutable.ListBuffer
import org.squery.Table
import org.squery.sql.Update
import org.squery.sql.Delete
import org.squery.sql.Insert
import scala.collection.mutable.HashMap

object Process {
	
	var props:Properties  = new Properties();
	props.put("serializer.class", "org.squery.index.SelectEncoder");
	props.put("zk.connect", "192.168.10.84:2181");
	var config:ProducerConfig = new ProducerConfig(props);
	var producer:Producer[String,Array[Byte]]  = new Producer[String,Array[Byte]](config);

	
	def sendMessage(page:Page,cl:List[Column]
		,update:ListBuffer[Tuple2[Column,Any]],sl:Ansi,obj:Any){
		if(obj==null){
			return;
		}
		var index:SelectIndex.Builder = SelectIndex.newBuilder;
		var select:SelectIndex.SelectSQL.Builder = SelectIndex.SelectSQL.newBuilder;
		select.setSql(sl.sql.toString);
		select.setKey(processKey(sl.sql.toString,sl));
		if(page!=null){
			select.setType(SelectType.PAGE);
			select.setPnum(page.getPageNo);
		}else{
			select.setType(SelectType.DEFAULT);
			select.setPnum(0);
		}
		sl.tTable.foreach(i => select.addTable(i.table));
		cl.foreach(i => select.addSelect(i.qName ));
		if(update!=null){
			for(i <- update){
				var d:Data.Builder = Data.newBuilder;
				d.setTable(i._1.table.table);
				d.setColumn(i._1.name);
				d.setMath(Assigment.Eq);
				setting(d,i._2);
				select.addUpdate(d);
			}
		}
		
		var mp:HashMap[String,CBaseOp] = new HashMap[String,CBaseOp]();
		var mv:HashMap[String,ListBuffer[Column]] = new HashMap[String,ListBuffer[Column]](); 
		for(i <- sl.sColumn){
			var d:Data.Builder = addWhere(i,mp,mv);
			if(d!=null){
				select.addWhere(d);
			}
		}
		
		for(i <- mv.keys){
			var v:CBaseOp = mp.get(i).getOrElse(null);
			if(v!=null){
				var l:ListBuffer[Column] = mv.get(i).getOrElse(null)
				finding(v,mp,l,select,null,mv);
			}
		}
		
		index.setSlsql(select);
		index.setSqlType(SQLType.SELECT);
		var o:ByteArrayOutputStream = new  ByteArrayOutputStream();
		index.build.writeTo(o);
		producer.send(new ProducerData[String,Array[Byte]]("sql",o.toByteArray));
		
		//MemCachedUtil.getCache.add(select.getKey,obj);
	}
	
	private def finding(b:CBaseOp,m:HashMap[String,CBaseOp],l:ListBuffer[Column],index:SelectIndex.SelectSQL.Builder
			,up:SelectIndex.WhereList.Builder
			,mv:HashMap[String,ListBuffer[Column]]){
		for(c <- l){
			if(m.get(c.qName).getOrElse(null)==null){
				var d:Data.Builder = addWhere(c,b);
				if(d!=null){
					if(index!=null){
						index.addWhere(d);
					}else if(up!=null){
						up.addWhere(d);
					}
					m.put(c.qName, b);
					var ll:ListBuffer[Column] = mv.get(c.qName).getOrElse(null)
					if(ll!=null){
						finding(b,m,ll,index,up,mv);
					}
				}
			}
		}
	}
	
	private def addWhere(op:CBaseOp,m:HashMap[String,CBaseOp],mv:HashMap[String,ListBuffer[Column]]):Data.Builder={
		var d:Data.Builder = null;
		
		  op match{
				case x:CEqual => 
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Eq);
						setting(d,x.t);
						m.put(x.c .qName , x)
					}else if(x.t.isInstanceOf[Column]){
						var l:ListBuffer[Column] = mv.get(x.c.qName).getOrElse(null);
						if(l==null){
							l = new ListBuffer[Column]();
							mv.put(x.c.qName , l);
						}
						var c:Column = x.t.asInstanceOf[Column]; 
						l += c
						l = mv.get(c.qName).getOrElse(null);
						if(l==null){
							l = new ListBuffer[Column]();
							mv.put(c.qName , l);
						}
						l += x.c
					}
				case x:CGe =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Ge);
						setting(d,x.t);
						m.put(x.c .qName , x)
					}
				case x:CGt =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Gt);
						setting(d,x.t);
						m.put(x.c .qName , x)
					}
				case x:CIn =>
					if(!x.t.isInstanceOf[List[_]]){
						var t:List[Any] = x.t.asInstanceOf[List[Any]];
						for(k <- t){
							d = Data.newBuilder;
							d.setTable(x.c.table.table);
							d.setColumn(x.c.name)
							d.setMath(Assigment.Eq);
							setting(d,k);
						}
					}
				case x:CLe =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Le);
						setting(d,x.t);
						m.put(x.c .qName , x)
					}
				case x:CLt =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Lt);
						setting(d,x.t);
						m.put(x.c .qName , x)
					}
				case _ =>
		  }
		return d;
	}
	
	private def addWhere(c:Column,op:CBaseOp):Data.Builder={
		var d:Data.Builder = null;
		
		  op match{
				case x:CEqual => 
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(c.table.table);
						d.setColumn(c.name)
						d.setMath(Assigment.Eq);
						setting(d,x.t);
					}
				case x:CGe =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Ge);
						setting(d,x.t);
					}
				case x:CGt =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Gt);
						setting(d,x.t);
					}
				case x:CIn =>
					if(!x.t.isInstanceOf[List[_]]){
						var t:List[Any] = x.t.asInstanceOf[List[Any]];
						for(k <- t){
							d = Data.newBuilder;
							d.setTable(x.c.table.table);
							d.setColumn(x.c.name)
							d.setMath(Assigment.Eq);
							setting(d,k);
						}
					}
				case x:CLe =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Le);
						setting(d,x.t);
					}
				case x:CLt =>
					if(!x.t.isInstanceOf[Column]){
						d = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Lt);
						setting(d,x.t);
					}
				case _ =>
		  }
		return d;
	}
	
	def sendMessage(up:Update,sql:Ansi){
		if(!sql.cacheable){
			return;
		}
		var index:SelectIndex.Builder = SelectIndex.newBuilder;
		var update:SelectIndex.UpdateSQL.Builder = SelectIndex.UpdateSQL.newBuilder;
		update.setTable(up.arg.table);
		for(i <- up.sets){
			 i match{	
				case x:CEqual => 
					if(!x.t.isInstanceOf[Column] && !x.t.isInstanceOf[CMath]){
						var d:Data.Builder = Data.newBuilder;
						d.setTable(x.c.table.table);
						d.setColumn(x.c.name)
						d.setMath(Assigment.Eq);
						setting(d,x.t);
						if(x.c.table._selecting!=null
								&& x.c.table._selecting.find(k => k.name == x.c.name).size>0){
							d.setUpdating(1);
						}
						update.addSet(d);
					}
				case _ =>
			 }
		}
		
		
		
		for(i <- sql.treeColumn){
			var wh:SelectIndex.WhereList.Builder = SelectIndex.WhereList.newBuilder; 
			var mp:HashMap[String,CBaseOp] = new HashMap[String,CBaseOp]();
			var mv:HashMap[String,ListBuffer[Column]] = new HashMap[String,ListBuffer[Column]](); 
			for(k <- i){
				var d:Data.Builder = addWhere(k,mp,mv);
				if(d!=null){
					wh.addWhere(d);
				}
			}
			for(i <- mv.keys){
				var v:CBaseOp = mp.get(i).getOrElse(null);
				if(v!=null){
					var l:ListBuffer[Column] = mv.get(i).getOrElse(null)
					finding(v,mp,l,null,wh,mv);
				}
			}
			update.addWlist(wh);
		}
		
		index.setUpsql(update)
		index.setSqlType(SQLType.UPDATE);
		var o:ByteArrayOutputStream = new  ByteArrayOutputStream();
		index.build.writeTo(o);
		producer.send(new ProducerData[String,Array[Byte]]("sql",o.toByteArray));
		
	}
	
	def sendMessage(up:Delete,sql:Ansi){
		if(!sql.cacheable){
			return;
		}
		var index:SelectIndex.Builder = SelectIndex.newBuilder;
		var delete:SelectIndex.DeleteSQL.Builder = SelectIndex.DeleteSQL.newBuilder;
		delete.setTable(up.arg.table);
		
		for(i <- sql.treeColumn){
			var wh:SelectIndex.WhereList.Builder = SelectIndex.WhereList.newBuilder; 
			var mp:HashMap[String,CBaseOp] = new HashMap[String,CBaseOp]();
			var mv:HashMap[String,ListBuffer[Column]] = new HashMap[String,ListBuffer[Column]](); 
			for(k <- i){
				var d:Data.Builder = addWhere(k,mp,mv);
				if(d!=null){
					wh.addWhere(d);
				}
			}
			for(i <- mv.keys){
				var v:CBaseOp = mp.get(i).getOrElse(null);
				if(v!=null){
					var l:ListBuffer[Column] = mv.get(i).getOrElse(null)
					finding(v,mp,l,null,wh,mv);
				}
			}
			delete.addWlist(wh);
		}
		
		index.setDlsql(delete)
		index.setSqlType(SQLType.DELETE);
		var o:ByteArrayOutputStream = new  ByteArrayOutputStream();
		index.build.writeTo(o);
		producer.send(new ProducerData[String,Array[Byte]]("sql",o.toByteArray));
	}
	
	def sendMessage(up:Insert,sql:Ansi){
		if(!sql.cacheable){
			return;
		}
		var index:SelectIndex.Builder = SelectIndex.newBuilder;
		var insert:SelectIndex.InsertSQL.Builder = SelectIndex.InsertSQL.newBuilder;
		insert.setTable(up.t.table);
		
		var count:Int =0;
		for(i <- up.c){
			var d:Data.Builder = Data.newBuilder;
			d.setTable(i.table.table);
			d.setColumn(i.name)
			d.setMath(Assigment.Eq);
			setting(d,up.vv.apply(count));
			if(up.t._selecting !=null 
					&& up.t._selecting.find(kk => kk.name == i.name).size>0){
				d.setUpdating(1);
			}
			insert.addIn(d)
			count += 1;
		}
		
		index.setInsql(insert);
		index.setSqlType(SQLType.INSERT);
		var o:ByteArrayOutputStream = new  ByteArrayOutputStream();
		index.build.writeTo(o);
		producer.send(new ProducerData[String,Array[Byte]]("sql",o.toByteArray));
	}
	
	
	private def setting(d:Data.Builder,i:Any){
		i match{
				case x:String => 
					d.setValue(x);
					d.setType(DataType.STRING)
				case x:Int => 
					d.setValue(x.toString);
					d.setType(DataType.INTEGER)
				case x:Long =>  
					d.setValue(x.toString);
					d.setType(DataType.LONG);
				case x:Float => 
					d.setValue(x.toString);
					d.setType(DataType.FLOAT)
				case x:Double => 
					d.setValue(x.toString);
					d.setType(DataType.DOUBLE)
				case x:Date => 
					d.setValue(java.lang.Long.toString(x.getTime))
					d.setType(DataType.DATE)
				case _ =>
		}
	}
	

	def getCache(sl:Ansi):Any={
		MemCachedUtil.getCache.get(processKey(sl.sql.toString,sl));
	}
	
	private def processKey(sql:String,sl:Ansi):String={
		var sb:StringBuilder = new StringBuilder();
		sb.append(sql);
		sl.params.foreach(i => sb.append(i));
		return JavaMD5.getMD5Str(sb.toString, "UTF-8")
	}
	
}