package org.squery.test
import org.squery.Table
import org.squery.Column
import org.squery.OneToOne
import org.squery.OneToMany
import org.squery.ManyToMany
import org.squery.MColumn
import scala.reflect.BeanProperty
import scala.collection.mutable.ListBuffer
import java.io.Serializable

@SerialVersionUID(83604588105013708L)
class User extends Serializable{
	@BeanProperty var id:Int = 0;
	@BeanProperty var name:String = "";
	@BeanProperty var sex:String = "";
	@BeanProperty var addressId:Int = 0;
	@BeanProperty var jobId:Int = 0;
	
	@BeanProperty var address:Address = null;
	@BeanProperty var jobs:java.util.List[Jobs] = new java.util.ArrayList[Jobs]();
}

object User extends Table("user"){
	var id:Column = "id";
	var name:Column = "name";
	var sex:Column = "sex";
	
	var addressId:Column = "addressid";
	var jobId:Column = "jobid";
	
	_relation = OneToOne(MColumn(User.addressId),MColumn(Address.id),"address") :: OneToMany(MColumn(User.id),MColumn(Par.userid),"jobs") :: ManyToMany(MColumn(User.id),MColumn(Jobs.id),"jobs") :: Nil
	_clazz = classOf[User];
	_pk = MColumn(User.id);
	_updating = id :: Nil;
	_selecting = id :: Nil;
}