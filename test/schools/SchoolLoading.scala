package schools

import org.scalatestplus.play._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test.Helpers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by stitch on 21/11/15.
  */
class SchoolLoading extends PlaySpec with Results {

  "the schools object" should {

    import play.api.libs.ws.ning._
    implicit val sslClient: WSClient = NingWSClient()

    val result: Future[String] = Schools.getPageForCode("RMIS013006")
    val content = Await.result(result, 3.seconds)

    "recover the school data" in {


      content.trim must startWith("<!DOCTYPE html>")
      content must include("""<table class="sc-table" id="tabellaRisultati">""")

    }

    "read a link from within a string value" in {
      val map = """VIA CAVOUR, 258 (<a target="_new" href="http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA">mappa</a>)"""
      Schools.readLink(map) must be("http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA")
    }

    "extract the school information from the raw page" in {
      val school: Option[School]= Parser.readSchoolFromPage(content)

      school mustBe defined
      school.get.code must be("RMIS013006")
      school.get.name must be("LEONARDO DA VINCI")
      school.get.link must be("""/cercalatuascuola/istituti/RMIS013006/leonardo-da-vinci/""")
      school.get.address must be("VIA CAVOUR 258 ROMA, ROMA (RM)")
      school.get.map mustBe empty
      /*
      school.schoolType must be("SCUOLA SECONDARIA DI SECONDO GRADO STATALE")
      school.city must be("ROMA")
      school.province must be("RM")
      */
    }

  }

}
