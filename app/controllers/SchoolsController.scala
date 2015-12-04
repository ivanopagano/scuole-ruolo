/**
  * Created by Ivano Pagano on 22/11/15.
  */
package controllers

import javax.inject.Inject
import com.typesafe.config.ConfigFactory
import schools.{Schools, Parser, School}

import scala.concurrent.Future
import scala.collection.JavaConversions._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import schools.Schools._

class SchoolsController @Inject() (ws: WSClient) extends Controller {

  lazy val allCodes: List[String] = ConfigFactory.load().getStringList("schools.codes").toList

  /**
    * json api for single element
    * @param code
    * @return
    */
  def school(code: String) = Action.async {
    val school: Future[Option[School]] = loadSchool(code)
    school map {
      case Some(s) => Ok(Json.toJson(s))
      case None    => NotFound
    }
  }

  def schools = Action.async {
    val results: Future[List[Option[School]]]= Future.sequence(allCodes map loadSchool)
    results map {
      schoolList =>
        val sorted = Schools sorting (schoolList.flatten)
        Ok(views.html.schools.list(sorted))
    }
  }

  private def loadSchool(code: String): Future[Option[School]] = {
    val page = getPageForCode(code)(ws)
    val school = page map (Parser.readSchoolFromPage)
    school
  }
}
