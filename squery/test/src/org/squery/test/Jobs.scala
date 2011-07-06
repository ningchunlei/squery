package org.squery.test
import org.squery.Table
import org.squery.Column
import org.squery.OneToOne
import org.squery.MColumn
import scala.reflect.BeanProperty
import java.io.Serializable

class Jobs extends Serializable{
	
	@BeanProperty var id:Int = 0;
	@BeanProperty var name:String = null;
	@BeanProperty var companyId:Int = 0;
	
	@BeanProperty var company:Company = null;
}

object Jobs extends Table("jobs"){
	var id:Column = "id";
	var name:Column = "name";
	var companyId:Column = "companyid";
	
	_relation = OneToOne(MColumn(Jobs.companyId),MColumn(Company.id),"company") :: Nil
	_clazz = classOf[Jobs];
	_pk = MColumn(Jobs.id);
}