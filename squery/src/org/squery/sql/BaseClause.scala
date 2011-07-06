package org.squery.sql
import org.squery.op.CBaseOp
import org.squery.sql.dialect.Ansi
import org.squery.Table
import javax.sql.DataSource
import java.sql.Connection
import java.sql.Statement
import java.sql.PreparedStatement
import java.sql.ResultSet
import org.squery.Column
import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.collection.JavaConversions._
import org.squery.op.CEqual
import org.squery.RTree
import org.squery.RNode
import java.lang.reflect.Field
import org.squery.SqlRef
import org.squery.Relation
import org.squery.OneToOne
import org.squery.util.Primitive
import org.squery.util.Mapping
import scala.collection.mutable.ListBuffer
import org.squery.util.SQueryBeanFactory
import org.squery.util.SourceFactoryAware
import org.squery.OneToMany
import org.squery.ManyToOne
import org.squery.ManyToMany
import org.squery.index.Process
import org.squery.Page
import org.squery.sql.dialect.Mysql
import java.sql.Date
import java.sql.Timestamp

abstract class BaseClause {
	def execute(sql:Ansi);
	protected var clist:ListBuffer[BaseClause] = new ListBuffer[BaseClause]();

	private var m_t:Table = null;
	private var m_clazz:Class[_] = null;
	private var m_type = 0;
	private var m_mapping:Mapping = null;
	private var m_statement:Statement = null;
	private var m_connection:Connection= null;
	private var m_batch:Int = 0;
	private var m_batchL:List[_] = null;
	private var m_master:Boolean = false;
	private var m_partiontable:String = null;
	private var m_partion:HashMap[String,AnyRef] = null;
	private var m_cache = false;
	private var m_sl:List[Column]= null;
	private var m_updating:ListBuffer[Column] = null;
	private var m_ordering:ListBuffer[Column] = ListBuffer[Column]();
	private var m_uprs:ListBuffer[Tuple2[Column,Any]] = null;
	private var m_page:Page = null;

	def anything(c:String):BaseClause={
		clist += (new AnyThing(c));
		return this;
	}

	def where(x:CBaseOp):BaseClause = {
		clist +=(new Where(x))
		return this;
	}

	def as(tb:String):Table = {
		return new Table(tb,this);
	}

	def groupby(x:Column*):BaseClause = {
		clist += (new GroupBy(x.toList));
		return this;
	}

	def having(x:CBaseOp):BaseClause ={
		clist += (new Having(x));
		return this;
	}

	def orderby(x:Column*):BaseClause = {
		clist += (new OrderBy(x.toList));
		m_ordering ++= x.toList 
		return this;
	}

	def limit(x:Int*):BaseClause= {
		clist += (new Limit(x.toList));
		return this;
	}

	
	def page(page:Page):BaseClause={
		if(page.getTotalCount>0){
			return this;
		}
		var s:Select = Select(Fun("count","*"));
		for(i <- clist){
			if(!i.isInstanceOf[OrderBy]){
				s.clist += i;
			}
		}
		var total:Int = s.mapping(classOf[Int],null,Select.single).exec(new Mysql()).asInstanceOf[Int];
		page.setTotalCount(total);
		m_page = page;
		return limit(page.getOffset,page.getPageSize);
	}
	
	def forupdate():BaseClause= {
		clist += (new ForUpdate());
		return this;
	}

	def partion(t:HashMap[String,AnyRef],table:String){
		m_partion = t;
		m_partiontable =table;
	}

	def mapping(m:Mapping):BaseClause={
		this.m_mapping = m;
		return this;
	}

	def mapping(m_clazz:Class[_],m_t:Table,m_type:Int):BaseClause={
		this.m_t = m_t;
		this.m_clazz = m_clazz;
		this.m_type = m_type;
		if(this.isInstanceOf[Select]){
			var s:Select = this.asInstanceOf[Select];
			var mt:ListBuffer[Column] = new ListBuffer[Column]();
			var cl:ListBuffer[Column] = new ListBuffer[Column]();
			var tbs:HashMap[String,String]=new HashMap[String,String]();
			for(i <- s.cl){
				if(tbs.get(i.table .table).size==0){
					tbs.put(i.table.table, "");
					mt += i;
				}
			}
			if(tbs.size>1){
				for(i <- mt){
					for(c <- i.table._pk.m){
						if(s.cl.find(f => f.table.table==c.table.table &&
								f.name == c.name).size==0){
							var co:Column = i.myCloneImpl();
							co.tb = i.tb;
							co.name = c.name;
							co.qName = String.format("%s.%s",i.tb,c.name);
							cl += co;
						}
					}
				}
				cl ++= s.cl;
				s.cl = cl.toList;
			}
		}
		return this;
	};

	def addbatch(b:Int,l:List[_]):BaseClause={
		m_batch = b;
		m_batchL = l;
		return this;
	}

	def cache(b:Boolean):BaseClause={
		m_cache = b;
		return this;
	}

	def exec(sql:Ansi):Any = {
		var res:Any = null;
		var rs:ResultSet = null;
		if(this.isInstanceOf[Select] && sql.cacheable){
			indexSelect(sql);
			execute(sql);
		}else{
			execute(sql);
		}
		try{
			if(this.isInstanceOf[Select]){
				res = Process.getCache(sql);
				if(res==null){
					rs = exeSelectSql(sql);
					res = mappingAfter(rs,sql);
					Process.sendMessage(m_page, this.asInstanceOf[Select].cl,m_uprs,sql, res)
				}
			}else if(this.isInstanceOf[Insert]
			   || this.isInstanceOf[Update] || this.isInstanceOf[Delete]){
				var c:Any=exeUpdateSql(sql);
				if(this.isInstanceOf[Insert]){
					Process.sendMessage(this.asInstanceOf[Insert], sql)
				}else if(this.isInstanceOf[Update]){
					Process.sendMessage(this.asInstanceOf[Update], sql)
				}else if(this.isInstanceOf[Delete]){
					Process.sendMessage(this.asInstanceOf[Delete], sql)
				}
				if(c.isInstanceOf[Int]){
					return c;
				}else if(c.isInstanceOf[List[_]]){
					return c;
				}else if(c.isInstanceOf[ResultSet]){
					rs = c.asInstanceOf[ResultSet];
					if(rs.next){
						res = rs.getInt(1);
					}
					rs.close();
				}
			}
		}finally{
			closesql(rs);
		}
		return res;
	}

	private def indexSelect(sql:Ansi):Unit={
	  if(sql.cacheable){
	 	m_updating = new ListBuffer[Column]();  
	    var s:Select = this.asInstanceOf[Select];
	    var list:ListBuffer[Column] = new ListBuffer[Column](); 
		m_sl = s.cl;
		list ++= s.cl;
		var m:HashMap[Table,ListBuffer[Column]] = new HashMap[Table,ListBuffer[Column]]()
		for(i <- s.cl){
			var k:Table = i.table;
			var t:ListBuffer[Column] = m.get(k).getOrElse(null);
			if(t==null){
				t = new ListBuffer[Column]();
				m.put(k, t);
			}
			t += i;
		}
		for(i <- m.keySet){
			var l:ListBuffer[Column] = m.get(i).getOrElse(null);
			var tmp:Column = l.apply(0);
			if(i._updating!=null){
				for(j <- i._updating){					
					tmp = tmp.copy(j.name);
					m_updating += tmp;
				}
			}
			
		}
		list ++= m_updating;
		list ++= m_ordering;
		s.cl = list.toList; 
	  }
	}

	private def mappingAfter(rs:ResultSet,sql:Ansi):Any={
		if(m_mapping!=null){
			return m_mapping.mapping(rs);
		}else{
			var rtree:RTree = new RTree();
			var s:Select = this.asInstanceOf[Select];
			var m:HashMap[String,Column] = new HashMap();
			
			if(m_sl!=null){
				s.cl = m_sl;
			}
			
			for(i <- s.cl){
				var k:String = i.tb +"."+i.table.table;
				if(m.get(k).getOrElse(null)==null){
					m.put(k, i);
				}
			}
			if(m.size>=2){
				var l:ListBuffer[Array[Column]] = new ListBuffer[Array[Column]]();
				findWhere(l,m);
				for(i <- l){
					rtree.insert(i.apply(0), i.apply(1));
				}
				rtree.removeNotWhole();
				val root:RNode = rtree.find(this.m_t);
				var sortList:ListBuffer[Column] = new ListBuffer[Column]();
				var buf:ListBuffer[RNode] = new ListBuffer[RNode]();
				sortColumn(s.cl,root,sortList,buf);
				s.cl = sortList.toList;
				return mappingMore(rs,root);
			}else{
				return mappingSingle(rs);
			}
		}
	}

	private def mappingMore(rs:ResultSet,root:RNode):Any={
		var s:Select = this.asInstanceOf[Select];
		var index:HashMap[String,AnyRef] = new HashMap[String,AnyRef];
		var indexData:HashMap[String,AnyRef] = new HashMap[String,AnyRef];
		var res:ListBuffer[Any] = new ListBuffer[Any];
		var td:Table = new Table("");
		while(rs.next){
			var table:Table = td;
			for(cl <- s.cl){
				var bean:AnyRef = null;
				if(table.table!=cl.table.table){
					bean = index.get(cl.table.table).getOrElse(null);
					if(bean==null){
						bean = cl.table._clazz.newInstance().asInstanceOf[AnyRef];
						index.put(cl.table .table, bean);
					}
					var sb:StringBuilder = new StringBuilder();
					for(c <- cl.table._pk.m){
						sb.append(getResult(rs,bean,c)).append("__");
					}
					var r:RNode = root.search(cl.table);
					bean = r.search(sb.toString);
					if(bean==null){
						bean = cl.table._clazz.newInstance().asInstanceOf[AnyRef];
						r.put(sb.toString,bean);
					}
					if(table.table ==""){
						res += bean;
					}else{
						var rtb:RNode = root.searchParent(cl.table,null);
						var relation:Relation = rtb.rm.get(cl.table.table).get.first.r;
						if(relation.isInstanceOf[OneToOne] || relation.isInstanceOf[OneToMany] || relation.isInstanceOf[ManyToOne]) {
							var prior = indexData.get(rtb.table.table).getOrElse(null);
							if(prior==null){
								rtb = root.searchParent(rtb.table,null);
								for(r <- rtb.table._relation){
									if(r.isInstanceOf[ManyToMany]){
										var rel:Relation =  r.asInstanceOf[ManyToMany];
										for(mc <- rel.that.m){
											if(mc.table.table==cl.table.table){
												relation = rel;
											}
										}
									}
								}
								prior = indexData.get(rtb.table.table).getOrElse(null);
							}
							var obj:Any = SqlRef.get(relation.ref, prior)

							if(obj.isInstanceOf[java.util.List[_]]){
								var objList:java.util.List[AnyRef] = obj.asInstanceOf[java.util.List[AnyRef]];
								objList += bean;
							}else{
								SqlRef.set(relation.ref, prior, bean);
							}
						}
					}


					indexData.put(cl.table .table, bean);
					table = cl.table;
				}

				bean = indexData.get(cl.table.table).getOrElse(null);
				setResult(rs,bean,cl);
			}
			if(m_updating!=null && m_updating.size>0){
				if(m_uprs==null)
					m_uprs = new ListBuffer[Tuple2[Column,Any]]();
				for(i <- m_updating){
					var bean:AnyRef = index.get(i.table.table).getOrElse(null);
					if(bean==null){
						bean = i.table._clazz.newInstance().asInstanceOf[AnyRef];
						index.put(i.table .table, bean);
					}
					var res:Any = getResult(rs,bean,i);
					var tp:Tuple2[Column,Any] = Tuple2[Column,Any](i,res);
					m_uprs += tp;
				}
				for(i <- m_ordering){
					var bean:AnyRef = index.get(i.table.table).getOrElse(null);
					if(bean==null){
						bean = i.table._clazz.newInstance().asInstanceOf[AnyRef];
						index.put(i.table .table, bean);
					}
					var res:Any = getResult(rs,bean,i);
					var tp:Tuple2[Column,Any] = Tuple2[Column,Any](i,res);
					m_uprs += tp;
				}
			}
			
		}
		if(m_type==Select.single){
			if(res.size==0){
				res = null;
			}else{
				return res.apply(0);
			}
		}
		return res;
	}



	private def setResult(rs:ResultSet,bean:Any,cl:Column)={
		result(rs,bean,cl,1);
	}

	private def result(rs:ResultSet,bean:Any,cl:Column,ty:Int):Any={
		var field:String = null;
		var fieldType:Class[_] = null;
		var r:Any = null;
		var f:Boolean = false;

		bean match {
			case x:String =>
				fieldType = classOf[String];
			case x:Int =>
				fieldType = classOf[Int];
			case x:Long =>
				fieldType = classOf[Long];
			case x:Short =>
				fieldType = classOf[Short];
			case x:Float =>
				fieldType = classOf[Float];
			case x:Double =>
				fieldType = classOf[Double];
			case x:Date =>
				fieldType = classOf[Date];
			case x:Timestamp =>
				fieldType = classOf[Timestamp];
			case x:java.util.Date =>
				fieldType = classOf[java.util.Date];
			case x:AnyRef => 
				field = SqlRef.get(cl);
				fieldType = SqlRef.getType(field, x);
				f = true;
			case _ =>
		}

		var cn:String = cl.qName ;
		if(cl.asStr!=null){
			cn = cl.asStr ;
		}
		if(fieldType==classOf[Int]){
			r = rs.getInt(cn)
		}else if(fieldType==classOf[String]){
			r = rs.getString(cn)
		}else if(fieldType==classOf[Short]){
			r = rs.getShort(cn)
		}else if(fieldType==classOf[Long]){
			r = rs.getLong(cn)
		}else if(fieldType==classOf[Float]){
			r = rs.getFloat(cn)
		}else if(fieldType==classOf[Double]){
			r = rs.getDouble(cn)
		}else if(fieldType==classOf[Date]){
			r = rs.getDate(cn)
		}else if(fieldType==classOf[java.util.Date]){
			r = rs.getDate(cn)
		}else if(fieldType==classOf[Timestamp]){
			r = rs.getTimestamp(cn)
		}
		
		if(ty==1 && f){
			SqlRef.set(field, bean.asInstanceOf[AnyRef], r);
		}

		return r;
	}

	private def getResult(rs:ResultSet,bean:Any,cl:Column):Any={
		result(rs,bean,cl,0);
	}

	private def mappingSingle(rs:ResultSet):Any={
		var s:Select = this.asInstanceOf[Select];
		var res:ListBuffer[Any] = new ListBuffer[Any];
		var indexData:HashMap[String,Any] = new HashMap[String,Any];
	
		while(rs.next){
			indexData.clear();
			var bean:Any = null
			for(cl <- s.cl){
				if(m_clazz==null){
					m_clazz = cl.table ._clazz ;
				}
				bean = indexData.get(cl.table.table).getOrElse(null);
				if(bean==null){
					if(Primitive.isPrimitive(m_clazz)){
						bean = Primitive.get(m_clazz);
					}else{
						bean = m_clazz.newInstance().asInstanceOf[AnyRef];
					}
					indexData.put(cl.table .table, bean);
				}
				bean = indexData.get(cl.table.table).getOrElse(null);

				if(Primitive.isPrimitive(bean)){
					bean = getResult(rs,bean,cl);
				}else{
					setResult(rs,bean,cl);
				}

			}
			res += bean;
			
			if(m_updating!=null && m_updating.size>0){
				if(m_uprs==null)
					m_uprs = new ListBuffer[Tuple2[Column,Any]]();
				for(i <- m_updating){
					bean = indexData.get(i.table.table).getOrElse(null);
					if(bean==null){
						if(Primitive.isPrimitive(m_clazz)){
							bean = Primitive.get(m_clazz);
						}else{
							bean = m_clazz.newInstance().asInstanceOf[AnyRef];
						}
						indexData.put(i.table .table, bean);
					}
					var res:Any = getResult(rs,bean,i);
					var tp:Tuple2[Column,Any] = Tuple2[Column,Any](i,res);
					m_uprs += tp;
				}
				for(i <- m_ordering){
					bean = indexData.get(i.table.table).getOrElse(null);
					if(bean==null){
						if(Primitive.isPrimitive(m_clazz)){
							bean = Primitive.get(m_clazz);
						}else{
							bean = m_clazz.newInstance().asInstanceOf[AnyRef];
						}
						indexData.put(i.table .table, bean);
					}
					var res:Any = getResult(rs,bean,i);
					var tp:Tuple2[Column,Any] = Tuple2[Column,Any](i,res);
					m_uprs += tp;
				}
			}
			
		}
		
		
		
		if(m_type==Select.single){
			if(res.size==0){
				res = null;
			}else{
				return res.apply(0);
			}
		}
		return res;
	}

	private def sortColumn(cl:List[Column],root:RNode,res:ListBuffer[Column],buf:ListBuffer[RNode]):Unit={
			if(buf.find(_==root).size>0){
				return null;
			}else{
				buf += root;
			}
			for(i <- cl){
				if(i.table.table == root.table.table ){
					res += i;
				}
			}
			for(c <- root.rt){
				sortColumn(cl,c,res,buf);
			}
	}

	private def findWhere(l:ListBuffer[Array[Column]],m:HashMap[String,Column]){
		for(i <- clist){
			if(i.isInstanceOf[Where]){
				var w:Where = i.asInstanceOf[Where];
				findRelation(w.opt,l,m);
			}else if(i.isInstanceOf[On]){
				var w:On = i.asInstanceOf[On];
				findRelation(w.opt,l,m);
			}
		}

	}

	private def findRelation(w:CBaseOp,l:ListBuffer[Array[Column]],m:HashMap[String,Column]):Unit={
		if(w.left!=null){
			findRelation(w.left,l,m);
		}

		w match {
			case e:CEqual =>
			if(e.t .isInstanceOf[Column]){
				var t:Column = e.t.asInstanceOf[Column];
				var c:Array[Column] = new Array[Column](2);
				c.update(0,e.c);
				c.update(1,t);
				l += c;

			}
			case _ =>
		}

		if(w.right!=null){
			findRelation(w.right,l,m);
		}
	}

	private def closesql(rs:ResultSet){
		if(rs!=null){
			rs.close();
		}
		if(m_statement!=null){
			m_statement.close();
			m_statement = null;
		}
		if(m_connection!=null){
			m_connection.close();
			m_connection = null;
		}

	}

	private def exeUpdateSql(sql:Ansi):Any={
		m_connection = connection();
		var rs:Any = null;
		if(sql.params.size==0){
			var st:Statement = statement(m_connection);
			m_statement= st;
			rs = st.executeUpdate(sql.sql.toString)
			if(this.m_clazz!=null){
				rs = st.getGeneratedKeys;
			}
		}else{
			var ps:PreparedStatement = prepareStatement(sql.sql.toString,m_connection);
			m_statement= ps;
			if(m_batch==0){
				var i:Int = 0;
				for(p <- sql.params){
					i += 1;
					if(p.isInstanceOf[String]){
						ps.setString(i,p.asInstanceOf[String]);
					}else if(p.isInstanceOf[Int]){
						ps.setInt(i, p.asInstanceOf[java.lang.Integer].intValue);
					}else if(p.isInstanceOf[Long]){
						ps.setLong(i, p.asInstanceOf[java.lang.Long].longValue);
					}else if(p.isInstanceOf[Short]){
						ps.setShort(i, p.asInstanceOf[java.lang.Short].shortValue);
					}else if(p.isInstanceOf[Float]){
						ps.setFloat(i, p.asInstanceOf[java.lang.Float].floatValue);
					}else if(p.isInstanceOf[Double]){
						ps.setDouble(i, p.asInstanceOf[java.lang.Double].doubleValue);
					}else if(p.isInstanceOf[Date]){
						ps.setDate(i, p.asInstanceOf[Date]);
					}else if(p.isInstanceOf[java.util.Date]){
						ps.setDate(i, p.asInstanceOf[Date]);
					}else if(p.isInstanceOf[Timestamp]){
						ps.setTimestamp(i, p.asInstanceOf[Timestamp]);
					}
				}

				rs = ps.executeUpdate
				if(this.m_clazz!=null){
					rs = ps.getGeneratedKeys;
				}
			}else{
				var rr:ListBuffer[Int] = new ListBuffer[Int]();
				var inc:Int = 0;
				for(ll <- m_batchL){
					var i:Int = 0;
					for(pp <- sql.params){
						var p:Any = null;
						i += 1;
						if(pp.isInstanceOf[Column]){
							var c:Column = pp.asInstanceOf[Column];
							p = SqlRef.get(SqlRef.get(c), ll.asInstanceOf[AnyRef]);
						}else{
							p = pp;
						}
						if(p.isInstanceOf[String]){
							ps.setString(i,p.asInstanceOf[String]);
						}else if(p.isInstanceOf[Int]){
							ps.setInt(i, p.asInstanceOf[java.lang.Integer].intValue);
						}else if(p.isInstanceOf[Long]){
							ps.setLong(i, p.asInstanceOf[java.lang.Long].longValue);
						}else if(p.isInstanceOf[Short]){
							ps.setShort(i, p.asInstanceOf[java.lang.Short].shortValue);
						}else if(p.isInstanceOf[Float]){
							ps.setFloat(i, p.asInstanceOf[java.lang.Float].floatValue);
						}else if(p.isInstanceOf[Double]){
							ps.setDouble(i, p.asInstanceOf[java.lang.Double].doubleValue);
						}else if(p.isInstanceOf[Date]){
							ps.setDate(i, p.asInstanceOf[Date]);
						}else if(p.isInstanceOf[java.util.Date]){
							ps.setDate(i, p.asInstanceOf[Date]);
						}else if(p.isInstanceOf[Timestamp]){
							ps.setTimestamp(i, p.asInstanceOf[Timestamp]);
						}
						
					}
					inc += 1;
					ps.addBatch();
					if(inc % m_batch ==0){
						rr ++= ps.executeBatch;
					}
				}
				if(inc%m_batch!=0){
					rr ++= ps.executeBatch;
				}
				rs = rr.toList;
			}
		}
		return rs;
	}

	private def exeSelectSql(sql:Ansi):ResultSet={
		m_connection = connection();
		var rs:ResultSet = null;
		if(sql.params.size==0){
			var st:Statement = statement(m_connection);
			m_statement= st;
			rs = st.executeQuery(sql.sql.toString);
		}else{
			var ps:PreparedStatement = prepareStatement(sql.sql.toString,m_connection);
			m_statement= ps;
			var i:Int = 0;
			for(p <- sql.params){
				i += 1;
				if(p.isInstanceOf[String]){
					ps.setString(i,p.asInstanceOf[String]);
				}else if(p.isInstanceOf[Int]){
					ps.setInt(i, p.asInstanceOf[java.lang.Integer].intValue);
				}else if(p.isInstanceOf[Long]){
					ps.setLong(i, p.asInstanceOf[java.lang.Long].longValue);
				}else if(p.isInstanceOf[Short]){
					ps.setShort(i, p.asInstanceOf[java.lang.Short].shortValue);
				}else if(p.isInstanceOf[Float]){
					ps.setFloat(i, p.asInstanceOf[java.lang.Float].floatValue);
				}else if(p.isInstanceOf[Double]){
					ps.setDouble(i, p.asInstanceOf[java.lang.Double].doubleValue);
				}else if(p.isInstanceOf[Date]){
					ps.setDate(i, p.asInstanceOf[Date]);
				}else if(p.isInstanceOf[java.util.Date]){
						ps.setDate(i, p.asInstanceOf[Date]);
				}else if(p.isInstanceOf[Timestamp]){
					ps.setTimestamp(i, p.asInstanceOf[Timestamp]);
				}
			}
			rs = ps.executeQuery();
		}
		return rs;
	}

	private def statement(con:Connection):Statement = {
		return  con.createStatement();
	}

	private def prepareStatement(sql:String,con:Connection):PreparedStatement = {

		return con.prepareStatement(sql);
	}

	private def connection():Connection={
		var ds:SourceFactoryAware = SQueryBeanFactory.getSourceFactory();
		var t:Int = 0;
		if(this.isInstanceOf[Select]){
			t = SourceFactoryAware.SELECT;
		}else if(this.isInstanceOf[Delete]){
			t = SourceFactoryAware.DELETE;
		}else if(this.isInstanceOf[Update]){
			t = SourceFactoryAware.UPDATE;
		}else if(this.isInstanceOf[Insert]){
			t = SourceFactoryAware.INSERT;
		}
		return ds.getDataSource(t,m_partiontable,m_partion,false).getConnection;
	}

}