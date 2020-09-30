package observatory

import java.nio.file.Paths
import java.time.LocalDate
import java.util

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType, StringType, StructType}

import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  val ss = startLocalSparkSession

  def startLocalSparkSession = SparkSession.builder()
    .appName("Capstone")
    .master("local[*]")
    .getOrCreate()

  val schemaObservations = new StructType()
    .add("STN", StringType, true)
    .add("WBAN", StringType, true)
    .add("Month", IntegerType, true)
    .add("Day", IntegerType, true)
    .add("Temperature", DoubleType, true)

  val schemaStations = new StructType()
    .add("STN", StringType, true)
    .add("WBAN", StringType, true)
    .add("Latitude", DoubleType, true)
    .add("Longitude", DoubleType, true)


  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {

    import ss.implicits._

    def convertToCelsius(farenheit: Double) = (farenheit - 32) / 1.8

    val observationDataset: Dataset[Row] = {
      ss.read
        .option("header", "false")
        .schema(schemaObservations)
        .csv(ss.sparkContext.parallelize(Source.fromInputStream(Source.getClass.getResourceAsStream(temperaturesFile)).getLines().toSeq, 1000).toDS())
        .filter(x => x.get(2) != null && x.get(3) != null && x.get(4) != null)
        .persist()
    }

    val stationDataset: Dataset[Row] = {
      ss.read
        .option("header", "false")
        .schema(schemaStations)
        .csv(ss.sparkContext.parallelize(Source.fromInputStream(Source.getClass.getResourceAsStream(stationsFile)).getLines().toSeq, 1000).toDS())
        .filter(x => x.get(2) != null && x.get(3) != null)
        .persist()
    }

    val data = observationDataset.join(stationDataset,
      observationDataset("STN") <=> stationDataset("STN") && observationDataset("WBAN") <=> stationDataset("WBAN"),
      "inner")
      .map(x => (x.getAs[Int]("Month"), x.getAs[Int]("Day"),
        x.getAs[Double]("Latitude"), x.getAs[Double]("Longitude"),
        convertToCelsius(x.getAs[Double]("Temperature"))))
      .collect()

    val res = data.map(x => (LocalDate.of(year, x _1, x _2),
      Location(x _3, x _4),
      x _5))
    res
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    records.groupBy(_ _2) map (x => (x _1, x._2.map(y => y._3).sum / x._2.size))
  }

  private def resourcePath(resource: String): String = {
    import java.net.URI
    import java.nio.file.FileSystems
    val uri = getClass.getResource(resource).getPath
    val array = uri.toString().split("!");
    val fs = FileSystems.newFileSystem(URI.create(array(0)), new util.HashMap[String, Object])
    fs.getPath(array(1)).toUri.toString
  }
}
