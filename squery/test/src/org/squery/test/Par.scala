package org.squery.test
import org.squery.Table
import org.squery.Column
import org.squery.OneToOne
import org.squery.OneToMany
import org.squery.ManyToOne
import org.squery.MColumn
import scala.reflect.BeanProperty
import scala.collection.mutable.ListBuffer

class Par {
	
	@BeanProperty var id:Int=0;
	@BeanProperty var userid:Int = 0;
	@BeanProperty var jobid:Int = 0;
	
	
}

object Par extends Table("par"){
	var id:Column = "id";
	var userid:Column = "userid";
	var jobid:Column = "jobid";
	
	_relation = ManyToOne(MColumn(Par.userid),MColumn(User.id),"") :: ManyToOne(MColumn(Par.jobid),MColumn(Jobs.id),"") :: Nil
	_clazz = classOf[Par];
	_pk = MColumn(Par.id)
	_selecting = userid :: Nil 
}