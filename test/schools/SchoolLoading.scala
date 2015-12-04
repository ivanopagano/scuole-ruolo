/**
  * Created by Ivano Pagano on 21/11/15.
  */
package schools

import org.scalatest._

import org.scalatestplus.play._
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

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
      school.get.link must be("""http://cercalatuascuola.istruzione.it/cercalatuascuola/istituti/RMIS013006/leonardo-da-vinci/""")
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

class SchoolSorting extends WordSpec with Matchers {

  "The school object" should {

    "extract the city name from the address with cap" in {

      val city = Schools cityFromAddress """VIA PAOLO BORSELLINO S.N.C., 00052 CERVETERI (RM)"""

      city should be("CERVETERI")
    }

    "extract the city name from the address with no cap" in {

      val city = Schools cityFromAddress """VIA CAVOUR 258 ROMA, ROMA (RM)"""

      city should be("ROMA")
    }

    "extract a compund city name from the address" in {

      val city = Schools cityFromAddress """VIA A. DE GASPERI, 8, 00018 PALOMBARA SABINA (RM)"""

      city should be("PALOMBARA SABINA")
    }

  }

}


