/**
 * 
 */
package org.squery.test
import org.squery.sql.Select
import org.squery.sql.Update
import org.squery.sql.dialect.Mysql
import org.squery.sql.Tb
import org.squery.op.LiteralV
import org.squery.sql.Exist
import org.squery.Table
import org.squery.SqlRef
import java.lang.reflect.Field
import org.squery.Column
import scala.collection.mutable.MutableList
import scala.collection.mutable.HashMap
import org.squery.util.Mapping
import java.sql.ResultSet
import org.squery.sql.Fun
import org.squery.sql.Insert
import org.squery.sql.Delete
import org.squery.sql.Delete


/** 
* Filename:    Test.scala <br>
* Description:  <br>
* Copyright:   Copyright (c)2011 <br>
* Company:     zhongsou<br>
* @author:     ningcl<br>
* @version:    1.0 <br>
* Create at:   2011-3-31 ����02:20:08 <br>
* <br>
* Modification History: <br>
* Date         Author      Version     Description <br>
* ------------------------------------------------------------------ <br>
* 2011-3-31      ningcl         1.0       1.0 Version <br>
*/ 
object Test {
	def main(args:Array[String]){
		var u:User = new User();
		u.addressId =2;
		u.name ="你好";
		u.sex="男";
		
		var u1:User = new User();
		u1.addressId =4;
		u1.name ="你好1";
		u1.sex="男1";
		
		
		var ll:List[User] = u :: u1 :: Nil;
		//var c:Class[_] = classOf[Int];
		//println (c == classOf[Int]);
		/*var l:List[User]=(Select(User.name)from(User)).mapping(classOf[User], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[User]].toList;
		l.foreach(i => println(i.name));*/
		
		/*var l:List[String]=(Select(User.name)from(User)).mapping(classOf[String], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[String]].toList;
		l.foreach(i => println(i));*/
		
		/*var l:List[Int]=(Select(User.id)from(User)).mapping(classOf[Int], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[Int]].toList;
		l.foreach(i => println(i));*/
		
		/*var l:Int=(Select(User.id)from(User)).mapping(new Mapping(){
			def mapping(rs:ResultSet):Any={
				if(rs.next){
					return rs.getInt("id");
				}
			}
		}).exec(new Mysql())
		.asInstanceOf[Int];
		println (l)*/
		
		
		/*var l:List[Int]=(Select(Fun("count",User.id))from(User)).mapping(classOf[Int], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[Int]].toList;
		l.foreach(i => println(i));*/
		
		/*var l:List[User]=(Select(User.name,Address.address)from(User,Address)where(User.addressId===Address.id and Address.id===2)).mapping(classOf[User], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[User]].toList;
		l.foreach(i => println(i.address.address ));*/
		
		/*var l:List[User]=(Select(User.name,Address.address)from(User)leftjoin(Address)on(User.addressId===Address.id)
				where(User.id===1)orderby(User.name)).mapping(classOf[User], User, Select.single ).exec(new Mysql())
		.asInstanceOf[MutableList[User]].toList;
		l.foreach(i => println(i.address.address));*/
		
		/*
		var c:List[Int] = (Insert(User)(User.name,User.sex,User.addressId)values(
				User.name,User.sex,User.addressId
		)).addbatch(2,ll).exec(new Mysql()).asInstanceOf[List[Int]];
		println(c);*/
		
		var c:Int = (Delete(User)where(User.id===1)).exec(new Mysql()).asInstanceOf[Int];
		println(c);
		
		/*Select(User.id)from(User)where(User.id === new LiteralV(10)
			and 
			(User.id === "t" or User.id === "c") and Exist (Select(User.id)from(User)))execute(new Mysql());
		Select(User.id).
		from(User).
		where(
				Exist (Select(User.id)from(User))
		)execute(new Mysql());*/
	}

}