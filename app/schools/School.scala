/**
  * Created by Ivano Pagano on 21/11/15.
  */
package schools

import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSClient

import scala.concurrent.Future

object Schools {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  implicit val schoolWrites = new Writes[School] {
    def writes(s: School) = Json.obj(
      "code" -> s.code,
      "name" -> s.name,
      "address" -> s.address,
      "map" -> s.map,
      "link" -> s.link
    )
  }

  private val base_url = ConfigFactory.load().getString("schools.search.url")

  def getPageForCode(code: String)(implicit ws: WSClient): Future[String] =
    ws.url(base_url)
      .withQueryString("rapida" -> code, "tipoRicerca" -> "RAPIDA", "gidf" -> "1")
      .get()
      .flatMap {
        case res if res.status == OK =>
          Future(res.body)
        case res =>
          Logger.error(s"I didn't reach the url. Response has been $res")
          Future.failed(new Exception(s"I didn't reach the url. Response has been $res"))
      }

  def readLink(s: String) = {
    val link = """http://[^"]+""".r
    link.findFirstIn(s).getOrElse("")
  }

  def cityFromAddress(addr: String) = {
    val re = """^.*(?:,|\d{5})([a-zA-Z\s]+)\s\(RM\)$""".r
    (re findFirstMatchIn addr) map (_.group(1).trim) getOrElse ""
  }

  def sorting(schools: Seq[School]) = (schools sortBy {
    s => (cityFromAddress (s.address), s.name)
  }).toList

}

case class School(
  code: String = "",
  name: String = "",
  address: String = "",
  map: String = "",
  link: String = ""
)