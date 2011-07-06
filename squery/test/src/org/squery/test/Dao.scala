package org.squery.test
import org.squery.sql.Select
import org.squery.sql.Insert
import org.squery.sql.Tb
import org.squery.sql.Update
import org.squery.sql.Delete
import scala.collection.mutable.MutableList
import org.squery.sql.dialect.Mysql
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import org.squery.Page


class Dao {
	
	private type javaList = java.util.List[User]
	private type scalaList = ListBuffer[User]
	
	def findUser(t:Int):User={
		(Select(User.name,User.sex,User.addressId,Jobs.name)from(User,Jobs,Par)
				where(User.id===t and  Par.jobid===Jobs.id and Par.userid===User.id))
		.mapping(classOf[User], User, Select.single ).exec(new Mysql())
		.asInstanceOf[User]
	}
	
	def findU(t:Int,p:Page):javaList={
		(Select(User.name,User.sex,User.addressId)from(User)orderby(User.id)page(p)
		)
		.mapping(classOf[User], User, Select.set ).exec(new Mysql())
		.asInstanceOf[scalaList]
	}
	
	def insertUser(u:User):Int={
		(Insert(User)(User.id,User.name,User.sex,User.addressId)values(u.id,u.name,u.sex,u.addressId))
		.mapping(classOf[Int], User, Select.single ).exec(new Mysql())
		.asInstanceOf[Int]
	}
	
	def deleteUser(t:Int):Int={
		(Delete(User)where(User.id===t))
		.exec(new Mysql())
		.asInstanceOf[Int]
	}
	
	def insertPars(u:Par):Int={
		(Insert(Par)(Par.jobid,Par.userid)values(u.jobid,u.userid))
		.mapping(classOf[Int], User, Select.single ).exec(new Mysql())
		.asInstanceOf[Int]
	}
	
	def updateUser(u:User):Int={
		(Update(User)set(User.name===u.name )where(User.id===u.id))
		.exec(new Mysql())
		.asInstanceOf[Int]
	}
	
	def insertJobs(j:Jobs):Int={
		(Insert(Jobs)(Jobs.name,Jobs.companyId)values(j.name,j.companyId))
		.mapping(classOf[Int], Jobs, Select.single ).exec(new Mysql())
		.asInstanceOf[Int]
	}
	
	def insertCompany(j:Company):Int={
		(Insert(Company)(Company.name)values(j.name))
		.mapping(classOf[Int], Company, Select.single ).exec(new Mysql())
		.asInstanceOf[Int]
	}
	
}