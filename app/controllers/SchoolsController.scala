package controllers

import javax.inject.Inject
import com.typesafe.config.ConfigFactory
import schools.School

import scala.concurrent.Future
import scala.collection.JavaConversions._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.json._
import schools.Schools._

/**
  * Created by stitch on 22/11/15.
  */
class SchoolsController @Inject() (ws: WSClient) extends Controller {

  lazy val allCodes: List[String] = ConfigFactory.load().getStringList("schools.codes").toList

  /**
    * json api for single element
    * @param code
    * @return
    */
  def school(code: String) = Action.async {
    val school: Future[School] = loadSchool(code)
    school map (s => Ok(Json.toJson(s)))
  }

  def schools = Action.async {
    val results: Future[List[School]]= Future.sequence(allCodes map loadSchool)
    results map {
      schoolList =>
      Ok(views.html.schools.list(schoolList))
    }
  }

  private def loadSchool(code: String): Future[School] = {
    val page = getPageForCode(code)(ws)
    val school = page map (readSchoolFromPage)
    school
  }
}
