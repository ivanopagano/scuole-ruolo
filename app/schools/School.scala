package schools

import net.ruippeixotog.scalascraper.browser.Browser
import play.api.mvc.Result
import play.api.mvc.Results._
import play.api.http.Status._
import play.api.libs.ws.WSClient
import play.api.Logger
import scala.concurrent.Future

/**
  * Created by stitch on 21/11/15.
  */
object Schools {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private val base_url = "http://blia.it/scuola/index.php"

  def getPageForCode(code: String)(implicit ws: WSClient): Future[Result] =
    ws.url(base_url)
      .withQueryString("d" -> code)
      .get()
      .map {
        case res if res.status == OK =>
          Ok(res.body)
        case res =>
          Logger.error(s"I didn't reach the url. Response has been $res")
          NotFound
      }

  def stripInTags(s: String): String =
    if (s contains "<") {
      s.foldLeft(("", false)) {
        case ((content, false), '<') => (content, true)
        case ((content, false), c) => (content + c, false)
        case ((content, true), '>') => (content, false)
        case ((content, true), _) => (content, true)
      }._1
    } else s


  def readSchoolFromPage(html: String) = {
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
    import org.jsoup.nodes.Element

    lazy val needed = Set(
      "Codice scuola",
      "Tipo",
      "Denominazione",
      "Indirizzo",
      "Comune",
      "Provincia"
    )

    def readFieldRaw(content: Option[Seq[String]]) = for {
      cs <- content
      field = cleanName(cs.head)
      if needed(field)
    } yield (field, cs.tail.head)

    def cleanName(name: String) = if (name contains "Denomina-") "Denominazione" else name

    def cleanedField(v: Option[(String, String)]) = v map {
      case (field, value) => (field, stripInTags(value))
    }

    // read the page
    val dom = Browser().parseString(html)

    // extract all <tr> with interesting data
    val rows = (dom >?> elementList("tr.scuola")).get

    // read each <tr> and extract a pair from the inner <td>s
    // options comes from unused fields
    val allPairs: Seq[Option[(String, String)]] = for {
      tr <- rows
      rawPairs = readFieldRaw(tr >?> texts("td"))
    } yield (cleanedField(rawPairs))

    val fields: Map[String, String] = allPairs
      .filterNot(_ == None)
      .map(_.get)
      .toMap
      .withDefaultValue("")

    School(
      code = fields("Codice scuola"),
      schoolType = fields("Tipo"),
      name = fields("Denominazione"),
      address = fields("Indirizzo"),
      map = "",
      city = fields("Comune"),
      province = fields("Provincia")
    )
  }
}

case class School(
  code: String,
  schoolType: String,
  name: String,
  address: String,
  map: String = "",
  city: String,
  province: String
)