package org.squery.custom;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.NamedList;
import org.squery.index.JavaMD5;
import org.squery.index.MemCachedUtil;
import org.squery.index.SelectData.SelectIndex;
import org.squery.index.SelectData.SelectIndex.*;


public class CustomSql {
	
	private static final Logger log = Logger.getLogger(CustomSql.class);
	private static final String url = Config.getProperty("url", "squery_custom");
	
	public static void select(SelectIndex.SelectSQL sql){
		SolrInputDocument doc = new SolrInputDocument();
		doc.addField("key", sql.getKey());
		doc.addField("sqlkey", JavaMD5.getMD5Str(sql.getSql(),"UTF-8"));
		doc.addField("sql", sql.getSql());
		doc.addField("pnum", sql.getPnum());
		HashMap<String,String> map = new HashMap<String,String>();
		for(String column : sql.getSelectList()){
			if(column.indexOf(".")>-1){
				doc.addField("column", column);
				doc.addField("table", column.substring(column.indexOf(".")));
				map.put(column.substring(column.indexOf(".")), "");
			}
		}
		
		for(Data column : sql.getUpdateList()){
			doc.addField(column.getTable()+"."+column.getColumn()+"_"+column.getType(), column.getValue());
			doc.addField("table", column.getTable());
		}
		if(sql.getWhereList().size()!=0){
			for(Data column : sql.getWhereList()){
				doc.addField(column.getTable()+"."+column.getColumn()+"_"+column.getType(), column.getValue());
				doc.addField("table", column.getTable());
			}
		}else{
			if(sql.getType()==SelectType.DEFAULT){
				for(Iterator it=sql.getTableList().iterator();it.hasNext();){
					String t = (String)it.next();
					doc.addField(t+"_TABLE", "1");
				}
				for(Iterator it = map.keySet().iterator();it.hasNext();){
					String table = (String)it.next();
					doc.addField(table+"_TABLE", "1");
				}
			}
		}
		try {
			getSolrServer(url).add(doc);
			getSolrServer(url).commit();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void update(SelectIndex index){
		try {
			getSolrServer(url).optimize();
		}catch(Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(index.getSqlType()==SQLType.DELETE){
			StringBuilder sb = new StringBuilder();
			if(index.getDlsql().getWlistList().size()==0){
				sb.append("table:"+index.getDlsql().getTable());
			}else{
				for(WhereList wh:index.getDlsql().getWlistList()){
					sb.setLength(0);
					for(Data data:wh.getWhereList()){
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
						if(data.getMath()==Assigment.Eq){
							sb.append(data.getValue());
						}else if(data.getMath()==Assigment.Ge){
							sb.append("{"+data.getValue()).append(" TO *}");
						}else if(data.getMath()==Assigment.Gt){
							sb.append("["+data.getValue()).append(" TO *]");
						}else if(data.getMath()==Assigment.Le){
							sb.append("{ * TO ").append(data.getValue()+" }");
						}else if(data.getMath()==Assigment.Lt){
							sb.append("[ * TO ").append(data.getValue()+" ]");
						}
						sb.append(" AND ");
					}
					if(sb.length()>0){
						sb.delete(sb.length()-5, sb.length());
					}
					remvoeWhere(sb.toString());
				}
			}
		}else if(index.getSqlType()==SQLType.INSERT){
			StringBuilder sb = new StringBuilder();
			for(Data data:index.getInsql().getInList()){
				if(data.getUpdating()==1){
					sb.setLength(0);
					if(data.getType()==DataType.STRING){
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":").append(data.getValue());
					}else{
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
						sb.append("["+data.getValue()).append(" TO *] AND ");
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
						sb.append("[ * TO ").append(data.getValue()+" ]");
					}
					remvoeWhere(sb.toString());
					sb.setLength(0);
					sb.append("pnum:[1 TO *] AND ");
					sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
					sb.append("["+data.getValue()+" TO *]");
					remvoeWhere(sb.toString());
				}
			}
			remvoeWhere(index.getInsql().getTable()+"_TABLE:1");
		}else if(index.getSqlType()==SQLType.UPDATE){
			StringBuilder sb = new StringBuilder();
			if(index.getUpsql().getWlistList().size()==0){
				sb.append("table:"+index.getDlsql().getTable());
				sb.append(" AND ");
				sb.append(" (");
				for(Data data:index.getUpsql().getSetList()){
					sb.append("column:");
					sb.append(data.getTable()+"."+data.getColumn());
					sb.append(" OR ");
				}
				sb.delete(sb.length()-4, sb.length());
				sb.append(" )");
				remvoeWhere(sb.toString());
			}
			for(WhereList wh:index.getUpsql().getWlistList()){
				sb.setLength(0);
				for(Data data:wh.getWhereList()){
					sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
					if(data.getMath()==Assigment.Eq){
						sb.append(data.getValue());
					}else if(data.getMath()==Assigment.Ge){
						sb.append("{"+data.getValue()).append(" TO *}");
					}else if(data.getMath()==Assigment.Gt){
						sb.append("["+data.getValue()).append(" TO *]");
					}else if(data.getMath()==Assigment.Le){
						sb.append("{ * TO ").append(data.getValue()+" }");
					}else if(data.getMath()==Assigment.Lt){
						sb.append("[ * TO ").append(data.getValue()+" ]");
					}
					sb.append(" AND ");
				}
				
				sb.append(" (");
				for(Data data:index.getUpsql().getSetList()){
					sb.append("column:");
					sb.append(data.getTable()+"."+data.getColumn());
					sb.append(" OR ");
				}
				sb.delete(sb.length()-4, sb.length());
				sb.append(" )");
				remvoeWhere(sb.toString());
		   }
			
			for(Data data:index.getUpsql().getSetList()){
				if(data.getUpdating()==1){
					sb.setLength(0);
					if(data.getType()==DataType.STRING){
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":").append(data.getValue());
					}else{
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
						sb.append("["+data.getValue()).append(" TO *] AND ");
						sb.append(data.getTable()+"."+data.getColumn()+"_"+data.getType()).append(":");
						sb.append("[ * TO ").append(data.getValue()+" ]");
					}
					remvoeWhere(sb.toString());
				}
			}
			
		}
	}
	
	private static void remvoeWhere(String squery){
		SolrQuery query = new SolrQuery();
		query.setQuery(squery);
		query.addField("key,sql,pnum");
		QueryResponse result = null;
		try {
			List idlist = new ArrayList();
			result = getSolrServer(url).query(query);
			NamedList<Object> response = result.getResponse();
			SolrDocumentList list = (SolrDocumentList) response.get("response");
			for(SolrDocument doc:list){
				MemCachedUtil.getCache().remove((String)doc.getFieldValue("key"));
				idlist.add((String)doc.getFieldValue("key"));
				if(idlist.size()>300){
					getSolrServer(url).deleteById(idlist);
					getSolrServer(url).commit();
					idlist.clear();
				}
				if((Integer)doc.getFieldValue("pnum")>0){
					SolrQuery qy= new SolrQuery();
					qy.setQuery("sqlkey:"+doc.get("sqlkey")+" AND pnum:["+doc.getFieldValue("pnum")+" TO *]");
					qy.addField("key");
					QueryResponse prs = getSolrServer(url).query(qy);
					SolrDocumentList clist = (SolrDocumentList) response.get("response");
					for(SolrDocument cdoc:clist){
						MemCachedUtil.getCache().remove((String)cdoc.getFieldValue("key"));
						idlist.add((String)doc.getFieldValue("key"));
						if(idlist.size()>300){
							getSolrServer(url).deleteById(idlist);
							getSolrServer(url).commit();
							idlist.clear();
						}
					}
				}
			}
			if(idlist.size()>0){
				getSolrServer(url).deleteById(idlist);
				getSolrServer(url).commit();
				idlist.clear();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}
	
	private static CommonsHttpSolrServer getSolrServer(String url) {
		CommonsHttpSolrServer server = null;
		try {
			server = new CommonsHttpSolrServer(url);
			server.setSoTimeout(3000); // socket read timeout
			server.setConnectionTimeout(1000);
			server.setDefaultMaxConnectionsPerHost(1000);
			server.setMaxTotalConnections(10);
			server.setFollowRedirects(false); // defaults to false
			server.setAllowCompression(true);
			server.setMaxRetries(1);
		}
		catch (MalformedURLException e) {
			log.error(e);
		}
		return server;
	}
	
}
