package org.squery.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class DefaultSourceFactory implements SourceFactoryAware{
	
	private HashMap<String,DataSource> ds = new HashMap<String,DataSource>();
	private DataSource[] read_ds = null;
	private DataSource write_ds = null;
	private AtomicLong count = new AtomicLong(0);
	
	@Override
	public DataSource getDataSource(int op, String table, Map<String,Object> partion,boolean master) {
		// TODO Auto-generated method stub
		switch(op){
			case SELECT: 
				break;
			case INSERT:
			case DELETE:
			case UPDATE:
				return write_ds;
		}
		if(master){
			return write_ds;
		}
		
		ConnectionHolder conHolder = (ConnectionHolder)TransactionSynchronizationManager.getResource(write_ds);
		if(conHolder!=null && conHolder.isSynchronizedWithTransaction()){
			return write_ds;
		}
		long index = count.incrementAndGet();
		return read_ds[(int)(index % read_ds.length)];
	}

	public HashMap<String, DataSource> getDs() {
		return ds;
	}

	public void setDs(HashMap<String, DataSource> ds) {
		this.ds = ds;
		Set keys = ds.keySet();
		List<DataSource> read = new ArrayList<DataSource>();
		List<DataSource> write = new ArrayList<DataSource>();
		for(Iterator it=keys.iterator();it.hasNext();){
			String key = (String)it.next();
			if(key.startsWith("read_")){
				read.add(ds.get(key));
			}else if(key.startsWith("write_")){
				write.add(ds.get(key));
			}
		}
		read_ds = new DataSource[read.size()];
		read.toArray(read_ds);
		write_ds = write.get(0);
	}
	
	
	
	
}
