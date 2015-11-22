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

  "the schooloader " should {

    "recover the school data" in {

      import play.api.libs.ws.ning._
      implicit val sslClient: WSClient = NingWSClient()

      val result: Future[Result] = School.getDataForCode("RMIS013006")

      status(of = result) must be(OK)
      header(header = "content-type", of = result) must be(Some("text/plain; charset=utf-8"))

      val body = contentAsString(result)

      body must startWith("<!DOCTYPE html>")
      body must include("""<tr class="scuola">""")

    }

  }

}
