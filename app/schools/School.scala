package schools

import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.http.Status._
import play.api.libs.ws.WSClient
import play.api.Logger
import views.html.defaultpages.notFound
import scala.concurrent.Future

/**
  * Created by stitch on 21/11/15.
  */
class School(ws: WSClient) {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val base_url = "http://blia.it/scuola/index.php"

  def getDataForCode(code: String): Future[Result] = {
      ws.url(base_url)
        .withQueryString("d" -> code)
        .get()
        .map {
          case res if res.status == OK =>
            Ok(res.body)
          case res                     =>
            Logger.error(s"I didn't reach the url. Response has been $res")
            NotFound
      }
  }

}

object School {
  def getDataForCode(code: String)(implicit ws: WSClient) = new School(ws).getDataForCode(code)
}