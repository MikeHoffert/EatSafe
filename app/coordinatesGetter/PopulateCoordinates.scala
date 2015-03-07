package coordinatesGetter

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try
import anorm.SQL
import globals.ActiveDatabase
import play.api.Play.current
import play.api.db.DB
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.Reads
import play.api.libs.ws.WS
import play.api.libs.ws.WSRequestHolder
import play.api.mvc.Controller
import play.api.libs.json.JsResult
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Represent a coordinate, which contains a latitude and longitude
 * @param lat The latitude of this coordinate
 * @param long The longitude of this coordinate
 */
case class Coordinate(lat: Double, long: Double)


/**
 * Represent a location, that doesn't have coordinate in the database, and need to get the coordinate from Google Map API.
 * Use both address and city to find the exact coordinate
 * @param id The unique of this location
 * @param address The address of this location
 * @param city The city this location is in
 */
case class NoCoordinateLocation(id: Int, address: String, city: String)



object PopulateCoordinates extends Controller{

  /**
   * The Google Map API URL that accept HTTP get request and return a JSON object that contains 
   * the information and coordinate of each search result.
   * Should only use the first result.
   * Use "address + city" as parameter
   * eg. https://maps.googleapis.com/maps/api/geocode/json?address=101+Cumberland+Ave,+Saskatoon
   */
  var GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json"
  
  
  /**
   * Read a coordinate object from JSON
   * Only read the first result, which is index 0
   */
  implicit val coordinateReads: Reads[Coordinate] = (
    (JsPath \ "geometry" \\ "location" \ "lat").read[Double] and
    (JsPath \ "geometry" \\ "location" \ "lng").read[Double]
  )(Coordinate.apply _)
    

  /**
   * Update the coordinates of those locations that already have a corresponding coordinate stored in the 
   * "coordinate" backup table. This will be run first after each time the database is re-created.
   * @param connection The database connection 
   */
  def updateFromBackup()(implicit connection: java.sql.Connection): Unit = {
    
    val move = SQL(
        """
          UPDATE location
          SET latitude = coordinate.latitude,
          longitude = coordinate.longitude
          FROM coordinate
          WHERE location.address = coordinate.address
          AND location.city = coordinate.city;
        """
    ).execute()
  }
  
    
   /**
   * Update the coordinate in the location table
   * @param id The id of the location that the coordinate need to be updated
   * @param coordinate The coordinate for this location
   * @param connection The database connection 
   */
  def updateCoordinate(id: Int, coordinate: Coordinate)(implicit connection: java.sql.Connection): Unit = {
    
    val update = SQL(
       """
         UPDATE location SET latitude = """ + coordinate.lat + """, 
         longitude = """ + coordinate.long + """
         WHERE id = """ + id + """;
       """
    ).execute()
  }
  
   /**
   * Backup the coordinate, address, and city to "coordinate" table, whenever a new coordinate 
   * is got from Google Map API. 
   * @param connection The database connection 
   */
  def backupCoordinate(id: Int)(implicit connection: java.sql.Connection): Unit = {
    
    val backup = SQL(
       """
         INSERT INTO coordinate(address, city, latitude, longitude)
         SELECT address, city, latitude, longitude
         FROM location
         WHERE location.id = """ + id + """;
       """
    ).execute()
  }

  /**
   * Get a list of locations that don't have a coordinate (the latitude and longitude is null)
   * @param connection The database connection
   */
  def getNoCoordinateLocations()(implicit connection: java.sql.Connection): Try[Seq[NoCoordinateLocation]] = {

    Try {
      val query = SQL(
         """
           SELECT id, address, city
           FROM location
           WHERE location.latitude IS NULL
           AND location.longitude IS NULL
         """
      )
        
      query().map{
        row => NoCoordinateLocation(row[Int]("id"), row[String]("address"), row[String]("city"))
      }.toList
    }
  }
  
  def main()(implicit db: ActiveDatabase): Unit = {
    DB.withConnection { implicit connection =>  
      
      //First, update the coordinates from the backup "coordinate" table
      updateFromBackup()
      
      //Then get a list of locations that don't have coordinates in backup table
      getNoCoordinateLocations() match {
        case Success(locations) => 
          locations.map { location =>
            
            //Call the web service 
            val holder : WSRequestHolder = WS.url(GEOCODING_URL)
        
            //An execution context, required for Future.map:
            implicit val context = scala.concurrent.ExecutionContext.Implicits.global
            
            //The parameter for the HTTP get request
            //val parameterString = location.address + location.city;
            val parameterString = location.address + location.city;
            
            //for debug
            println(location.id + "*********" + parameterString)
            
            //Map the response JSON to a coordinate object
            val futureResult : Future[Coordinate] = holder.withQueryString("address" -> parameterString).get().map{
              response => (response.json \ "results" )(0).as[Coordinate]
            }
            
            
            futureResult.onComplete{
              case Success(coordinateResult) => 
                DB.withConnection { implicit connection =>  
                  val coordinate: Coordinate = coordinateResult
                  
                  println("id" + location.id+ " lat: "+ coordinate.lat + " long:" + coordinate.long)
                  //update this location
                  updateCoordinate(location.id, coordinate)
                  
                  //backup this new coordinate
                  backupCoordinate(location.id)
                }
                
              case Failure(ex) => println("Failed to get coordinate: "+ ex)
            } 
            println("waiting")
            Await.result(futureResult,Duration(99999999, "second"))
          }
        case Failure(ex) =>
          println("Failed to get no coordinates locations" + ex)
      }
    }
  }  
}