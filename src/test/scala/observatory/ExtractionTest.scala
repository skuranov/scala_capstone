package observatory

import org.junit.Assert._
import org.junit.Test

trait ExtractionTest extends MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  import Extraction._
  import Visualization._

  @Test def `testLocateTemperatures`: Unit = {

    val result = locateTemperatures(2015, "/stations_test.csv", "/temperatures_test.csv")

    assert(true, "does it work?")
  }

  @Test def `testLocationYearlyAverageRecords`: Unit = {

    val result = locationYearlyAverageRecords(
      locateTemperatures(2015, "/stations_test.csv", "/temperatures_test.csv"))
    assert(true, "does it work?")
  }


}
