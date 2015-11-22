package schools

import org.scalatestplus.play._
import play.api.libs.ws.WSClient
import play.api.mvc._
import play.api.test.Helpers._

import scala.concurrent.Future

/**
  * Created by stitch on 21/11/15.
  */
class SchoolLoading extends PlaySpec with Results {

  "the schools object" should {

    import play.api.libs.ws.ning._
    implicit val sslClient: WSClient = NingWSClient()

    val result: Future[Result] = Schools.getPageForCode("RMIS013006")

    "recover the school data" in {

      status(of = result) must be(OK)
      header(header = "content-type", of = result) must be(Some("text/plain; charset=utf-8"))

      val body = contentAsString(result)

      body must startWith("<!DOCTYPE html>")
      body must include("""<tr class="scuola">""")

    }

    "clean any string containing html tags" in {
      val noTag = "Roma"
      val single = "Denomina-<br>zione"
      val link = """<a href="index.php?p2=RM&amp;c=ROMA">ROMA</a>"""
      val map = """VIA CAVOUR, 258 (<a target="_new" href="http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA">mappa</a>)"""

      Schools.removeInnerTags(noTag) must be(noTag)
      Schools.removeInnerTags(single) must be("Denomina-zione")
      Schools.removeInnerTags(link) must be("ROMA")
      Schools.removeInnerTags(map) must be("VIA CAVOUR, 258 (mappa)")

    }

    "read a link from within a string value" in {
      val map = """VIA CAVOUR, 258 (<a target="_new" href="http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA">mappa</a>)"""
      Schools.readLink(map) must be("http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA")
    }

    "extract the school information from the raw page" in {

      val body = contentAsString(result)

      val school: School = Schools.readSchoolFromPage(body)

      school.code must be("RMIS013006")
      school.name must be("LEONARDO DA VINCI")
      school.schoolType must be("SCUOLA SECONDARIA DI SECONDO GRADO STATALE")
      school.address must be("VIA CAVOUR, 258 (mappa)")
      school.city must be("ROMA")
      school.province must be("RM")
      school.map must be("http://maps.google.it/maps?q=VIA%20CAVOUR,%20258%2000184%20ROMA")
    }

  }

}
