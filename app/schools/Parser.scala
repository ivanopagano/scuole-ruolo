/**
  * Created by Ivano Pagano on 22/11/15.
  */
package schools

import com.typesafe.config.ConfigFactory
import net.ruippeixotog.scalascraper.browser.Browser
import org.jsoup.nodes.Element
import play.api.Logger
import play.api.libs.ws.WSClient

import scala.concurrent.Future

object Parser {

  private lazy val baseUrl = ConfigFactory.load().getString("schools.home.url")

  def removeInnerTags(s: String): String =
    if (s contains "<") {
      s.foldLeft(("", false)) {
        case ((content, false), '<') => (content, true)
        case ((content, false), c) => (content + c, false)
        case ((content, true), '>') => (content, false)
        case ((content, true), _) => (content, true)
      }._1
    } else s


  def readSchoolFromPage(html: String): Option[School] = {
    import net.ruippeixotog.scalascraper.dsl.DSL._
    import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
    import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

    // read the page
    val dom = Browser().parseString(html)

    // get the table and extract the first table row
    val firstEntry: Option[Element] = for {
      table <- dom >?> element("table#tabellaRisultati")
      rows <- table >> element("tbody") >?> elementList("tr")
      first <- rows.headOption
    } yield first

    def readFields(tr: Element): Map[Symbol, Option[String]] =
      readPrincipal(tr >> element("td.sc-tab-istituto")) ++
      readAddress(tr >> element("td.sc-tab-indirizzo")) ++
      readMap(tr >> element("td.sc-tab-mappa"))

    def readPrincipal(e: Element): Map[Symbol, Option[String]] =
      Map(
        ('institute -> e >?> text("a")),
        ('code -> e >?> text(".sc-cod")),
        ('link -> (e >?> element("a"))
          .map(_.attr("href"))
          .map(baseUrl + _))
      )

    def readAddress(e: Element): Map[Symbol, Option[String]] =
    Map('address -> e >?> text("span"))

    def readMap(e: Element): Map[Symbol, Option[String]] =
    Map('map -> (e >?> element("a"))
      .map(_.attr("href"))
      .map(baseUrl + _))

    def toSchool(fields: Map[Symbol, Option[String]]) =
      School(
        code = fields('code).getOrElse(""),
        name = fields('institute).getOrElse(""),
        address = fields('address).getOrElse(""),
        map = fields('map).getOrElse(""),
        link = fields('link).getOrElse("")
    )

    firstEntry map (readFields _ andThen toSchool _)
  }

}
