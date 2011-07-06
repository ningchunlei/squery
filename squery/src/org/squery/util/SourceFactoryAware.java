package org.squery.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

public interface SourceFactoryAware {
	
	public static int SELECT=0;  
	public static int INSERT=1;
	public static int DELETE=2;
	public static int UPDATE=3;	

	public DataSource getDataSource(int op,String table,Map<String,Object> partion,boolean f);
	
}
