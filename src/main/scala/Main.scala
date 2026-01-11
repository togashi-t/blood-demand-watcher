import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import sttp.client4.*

import scala.jdk.CollectionConverters.*
import scala.util.Random

@main def Main(): Unit = {
  val urls = List(
    "https://www.bs.jrc.or.jp/hkd/bbc/",
    "https://www.bs.jrc.or.jp/th/bbc/",
    "https://www.bs.jrc.or.jp/ktks/bbc/",
    "https://www.bs.jrc.or.jp/tkhr/bbc/",
    "https://www.bs.jrc.or.jp/kk/bbc/",
    "https://www.bs.jrc.or.jp/csk/bbc/",
    "https://www.bs.jrc.or.jp/bc9/bbc/",
  )
  val backend = DefaultSyncBackend()

  try {
    val regionResultList = urls.zipWithIndex.map { case (url, index) =>
      if (index >= 1) Thread.sleep(Random.between(800, 1000))
      val response = basicRequest.get(uri"$url").response(asStringAlways).send(backend)
      val doc = Jsoup.parse(response.body)
      val title = extractTitle(doc)
      val bloodDemandStatusList = extractBloodDemandStateList(doc)
      RegionResult(url, title, bloodDemandStatusList)
    }
    regionResultList.foreach(printRegionResult)
  } finally {
    backend.close()
  }
}

private def extractTitle(doc: Document): String = doc.select("title").text().trim

private def extractBloodDemandStateList(doc: Document): List[BloodDemandState] = {
  doc.select(
    "ul.block-main-today-types.mod-tabSecs-item.on li.block-main-today-types-item"
  ).asScala.toList.flatMap(extractBloodDemandState)
}

private def extractBloodDemandState(li: Element): Option[BloodDemandState] = {
  for {
    bloodTypeText <- Option(li.selectFirst(".block-main-today-types-name span"))
      .map(_.text().trim)
    bloodType <- BloodType.fromString(bloodTypeText)
    state <- Option(li.selectFirst(".block-main-today-types-state"))
      .map(_.text().trim)
  } yield BloodDemandState(bloodType, state)
}

private def printRegionResult(r: RegionResult): Unit =
  println(s"\n=== ${r.title} ===")
  println(s"URL: ${r.url}")
  r.bloodDemandStateList.foreach { s =>
    val label = s.bloodType.toString
    println(f"  ${label}%-2s: ${s.state}")
  }

enum BloodType {
  case A, O, B, AB
}

object BloodType {
  def fromString(s: String): Option[BloodType] =
    s.trim match
      case "A" => Some(BloodType.A)
      case "O" => Some(BloodType.O)
      case "B" => Some(BloodType.B)
      case "AB" => Some(BloodType.AB)
      case _ => None
}

case class BloodDemandState(bloodType: BloodType, state: String)

case class RegionResult(url: String, title: String, bloodDemandStateList: List[BloodDemandState])