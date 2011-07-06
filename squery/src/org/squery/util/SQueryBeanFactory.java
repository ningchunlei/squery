package org.squery.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SQueryBeanFactory implements BeanFactoryAware{
	
	private static SQueryBeanFactory factory = new SQueryBeanFactory();
	private BeanFactory beanFactory = null;
	private static SourceFactoryAware df = null;
	
	public static SQueryBeanFactory getFactory(){
		return factory;
	}
	
	@Override
	public void setBeanFactory(BeanFactory arg0) throws BeansException {
		// TODO Auto-generated method stub
		beanFactory = arg0;
		df = beanFactory.getBean(SourceFactoryAware.class);
	}
	
	
	public static SourceFactoryAware getSourceFactory(){
		return df;
	}

}
