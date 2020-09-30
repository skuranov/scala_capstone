package observatory

import observatory.Extraction.{locateTemperatures, locationYearlyAverageRecords}
import observatory.Visualization._
import org.junit.Assert._
import org.junit.Test

class VisualizationTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("raw data display", 2) _

  // Implement tests for the methods of the `Visualization` object

  @Test def `testInterpolateColors`: Unit = {
    //Incorrect predicted color: Color(255,0,63). Expected: Color(191,0,64) (scale = List((0.0,Color(255,0,0)), (1.0,Color(0,0,255))), value = 0.25)
     val rgb = List((60d, Color(255,255,255)),(32d,Color(255	,0	,0)),(12d,Color(255,255,0)),(0d,Color(0,255,255)),(-15d,Color(0,0,255)),(-27d,Color(255,0,255)),(-50d,Color(33,0,107)),(-60d,Color(0,0,0)))
    //val rgb = List((0.0, Color(255, 0, 0)), (1.0, Color(0, 0, 255)))

    /*    val result = interpolateColor(rgb, 10)
        val result5 = interpolateColor(rgb, -34)
        val result1 = interpolateColor(rgb, -99)
        val result2 = interpolateColor(rgb, 54)
        val result3 = interpolateColor(rgb, 10000)
        val result4 = interpolateColor(rgb, 23)*/

    val result = interpolateColor(rgb, 0.25)

    assert(true, "does it work?")
  }

  @Test def `testPredictTemperature`: Unit = {
    val locTemp = locationYearlyAverageRecords(
      locateTemperatures(2015, "/stations_test.csv", "/temperatures_test.csv"))
    val result = predictTemperature(locTemp, Location(0.0, 0.0))
    val result1 = predictTemperature(locTemp, Location(5.0, 5.0))
    val result2 = predictTemperature(locTemp, Location(10.0, 10.0))
    val result3 = predictTemperature(locTemp, Location(45.0, 45.0))
    val result4 = predictTemperature(locTemp, Location(50.0, 50.0))
    assert(true, "does it work?")
  }

  @Test def `testPredictTemperature1`: Unit = {
    val locTemp = List()
    val result = predictTemperature(locTemp, Location(0.0, 0.0))
    val result1 = predictTemperature(locTemp, Location(5.0, 5.0))
    val result2 = predictTemperature(locTemp, Location(10.0, 10.0))
    val result3 = predictTemperature(locTemp, Location(45.0, 45.0))
    val result4 = predictTemperature(locTemp, Location(50.0, 50.0))
    assert(true, "does it work?")
  }

  @Test def `testVisualize`: Unit = {
    val locTemp = locationYearlyAverageRecords(
      locateTemperatures(2015, "/stations_test.csv", "/temperatures_test.csv"))
    val rgb = List((45d, Color(255,255,255)),(-30d,Color(0,0,0)))
    val result = visualize(locTemp, rgb)
    result.output(new java.io.File("target/some-image.png"))
    assert(true, "does it work?")
  }

}
