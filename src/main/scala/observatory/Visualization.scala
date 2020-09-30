package observatory

import com.sksamuel.scrimage.{Image, Pixel}

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location     Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {

    def computeDistance(l1: Location, l2: Location): Double = {
      val earthRad = 6371
      val antiL2 = Location(-l2.lat, -Math.signum(l2.lat) * (180 - Math.abs(l2.lat)))

      def computeRad = {
        if (l1.lat == l2.lat && l1.lon == l2.lon) {
          0d
        }
        else if (l1.lat == antiL2.lat && l1.lon == antiL2.lon) {
          Math.PI
        }
        else {
          Math.acos(Math.sin(Math.toRadians(l1.lat)) * Math.sin(Math.toRadians(l2.lat))
            + Math.cos(Math.toRadians(l1.lat)) * Math.cos(Math.toRadians(l2.lat))
            * Math.cos(Math.toRadians(l1.lon) - Math.toRadians(l2.lon)))
        }
      }
      earthRad * computeRad
    }

    val p = 2

    def shep = {
      def innerFunc(distance: Double): Double = 1 / Math.pow(distance, p)

      val distTemp = temperatures.par.map(x => (computeDistance(x._1, location), x._2))
      val fullMatch = distTemp.filter(x => x._1 <=1)
      if (fullMatch.nonEmpty) {
        fullMatch.head._2
      }
      else {
        val sum1 = distTemp.map(x => innerFunc(x._1) * x._2).sum
        val sum2 = distTemp.map(x => innerFunc(x._1)).sum
        sum1 / sum2
      }
    }

    if (temperatures.exists(l => l._1 == location)) {
      temperatures.filter(l => l._1 == location).head._2
    }
    else
      shep
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value  The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val exactTemp = points.filter(x => x._1 == value)
    if (exactTemp.nonEmpty) {
      exactTemp.head._2
    }
    else {
      val maxTempColor = points.max(Ordering.by[(Temperature, Color), Temperature](_._1))
      val minTempColor = points.min(Ordering.by[(Temperature, Color), Temperature](_._1))
      if (value >= maxTempColor._1) maxTempColor._2
      else if (value <= minTempColor._1) minTempColor._2
      else {
        val biggermas = points.filter(x => x._1 >= value)
        val bigger = biggermas.min(Ordering.by[(Temperature, Color), Temperature](_._1))
        val smaller = points.filter(x => x._1 < value).max(Ordering.by[(Temperature, Color), Temperature](_._1))
        val multiplier = (value - smaller._1) / (bigger._1 - smaller._1)
        Color((smaller._2.red + (bigger._2.red - smaller._2.red) * multiplier).toInt,
          (smaller._2.green + (bigger._2.green - smaller._2.green) * multiplier).toInt,
          (smaller._2.blue + (bigger._2.blue - smaller._2.blue) * multiplier).toInt)
      }
    }
  }

  /**
    * @param temperatures Known temperatures
    * @param colors       Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    def getPixelCoordinate(point: (Int, Int)): Location = Location(point._1 - 180, point._2 - 90)
    def createPixel (point: (Int, Int)):Pixel ={
      val color = interpolateColor(colors,  predictTemperature(temperatures,getPixelCoordinate(point)))
      Pixel(color.red,color.green,color.blue, 255)
    }

    val targetAr = new Array[Pixel](64800)

    val matrix = for (i <- 0 until 360; j <- 0 until 180) yield (i, j)

    matrix.par.map(point => targetAr.update(point._1 + point._2 * 360, createPixel(point)))

    Image.apply(360, 180, targetAr)
  }

}

