package org.squery.test
import org.squery.Column
import org.squery.Table
import org.squery.MColumn
import scala.reflect.BeanProperty
import java.io.Serializable

class Address extends Serializable{
	
	@BeanProperty var id:Int = 0;
	@BeanProperty var address:String = "";

}

object Address extends Table("address"){
	
	var id:Column="id";
	var address:Column="address";
	
	_clazz = classOf[Address];
	_pk = MColumn(Address.id);
	
}