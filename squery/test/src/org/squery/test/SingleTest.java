package org.squery.test;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.squery.Page;

public class SingleTest{
	static ApplicationContext ctx = null;
	static Dao dao = null;
	@BeforeClass
	public static  void init(){
		String[] locations = {"Application.xml"};
		ctx = new FileSystemXmlApplicationContext(locations);
		dao = (Dao)ctx.getBean("dao");
	}
	
	
	
	@Test
	public void select(){
		//User u = dao.findUser(4);
		//println(u.getName());
		Page p = new Page(1,3);
		List list = dao.findU(0, p);
		println(list);
		if(list!=null){
			println("size="+list.size());
		}
		/*for(int i=0;i<u.getJobs().size();i++){
			Jobs j = (u.getJobs().get(i));
			println(j.getName());
			println(j.getCompany());
		}*/
	}
	
	@Test
	public void insertPar(){
		Par x = new Par();
		x.setJobid(1);
		x.setUserid(4);
		int c = dao.insertPars(x);
		println(c);
	}

	@Test
	public void insert(){
		User u = new User();
		u.setId(1);
		u.setName("hello");
		u.setSex("f");
		u.setAddressId(5);
		int c = dao.insertUser(u);
		println(c);
	}
	
	@Test
	public void delete(){
		int c = dao.deleteUser(20);
		println(c);
	}
	
	
	@Test
	public void update(){
		User u = new User();
		u.setId(20);
		u.setName("hello");
		u.setSex("f");
		u.setAddressId(5);
		int c = dao.updateUser(u);
		println(c);
	}
	
	
	@Test
	public void insertJob(){
		Jobs j = new Jobs();
		j.setName("IT");
		j.setCompanyId(1);
		int c = dao.insertJobs(j);
		println(c);
	}
	
	@Test
	public void insertCompany(){
		Company j = new Company();
		j.setName("ZhongSou");
		int c = dao.insertCompany(j);
		println(c);
	}
	
	private void println(Object c){
		System.out.println(c);
	}
}
