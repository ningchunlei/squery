package org.squery.test
import org.squery.Table
import org.squery.Column
import org.squery.OneToOne
import org.squery.MColumn
import scala.reflect.BeanProperty

class Company {
	
	@BeanProperty var id:Int = 0;
	@BeanProperty var name:String = null;
	
}

object Company extends Table("company"){
	var id:Column = "id";
	var name:Column = "name";
	
	_clazz = classOf[Company];
	_pk = MColumn(Company.id);
}