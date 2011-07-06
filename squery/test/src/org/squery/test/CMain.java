package org.squery.test;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;


public class CMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String[] locations = {"Application.xml"};
		ApplicationContext ctx = new FileSystemXmlApplicationContext(locations);
		Dao d = (Dao)ctx.getBean("dao");
		List<User> list = (List<User>)d.findUser(2);
		System.out.println(list);
	}

}
