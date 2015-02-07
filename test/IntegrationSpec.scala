import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {
  "Application" should {
    "run in a browser" in new WithBrowser {
      browser.goTo("/")
    }
  }
  
  this.ControllerModelViewIntegration()
  
  def ControllerModelViewIntegration(){
    /**
     * Need to be updated as the view pages are developed
     * Checks to make sure that a page is actually displayed when controller is called
     */
    "Controller" should {
  	  "show display location page when showLocation is called" in {
  		  val result = controllers.LocationController.showLocation(1)(FakeRequest())
  			status(result) must equalTo(OK)
  			contentType(result) must beSome.which(_ == "text/html")
  			contentAsString(result) must contain("display location")
  	  }
  
  	  "show find location page when findLocation is called" in {
  		  val result = controllers.LocationController.findLocation("Saskatoon")(FakeRequest())
  			status(result) must equalTo(OK)
  			contentType(result) must beSome.which(_ == "text/html")
  			contentAsString(result) must contain("find location")
  	  }
    }
    
    /**
     * Dependent on the view actually displaying information
     */
    "showLocation" should {
  	  "display information for valid id" in {
  		  val result = controllers.LocationController.showLocation(2)(FakeRequest())
        status(result) must equalTo(OK)
  			contentAsString(result) must contain("7 Eleven")
  			contentAsString(result) must contain("835 A Broadway AVE")
  			contentAsString(result) must contain("S7N 1B5")
  	  } 
  
  	  "diplay error for invalid id" in {
  		  val result = controllers.LocationController.showLocation(-1)(FakeRequest())
        status(result) must equalTo(OK)
  			contentAsString(result) must contain("WHATEVER ERROR PAGE GIVES IN THIS CASE")
  	  }     
    }
    
    /**
     * Dependent on the view actually showing information
     */
    "findLocation" should {
      "display information for valid city" in {
        val result = controllers.LocationController.findLocation("Saskatoon")(FakeRequest())
        status(result) must equalTo(OK)
        contentAsString(result) must contain("Saskatoon")
        contentAsString(result) must contain("7 Eleven")
      }
      
      "display error message for ivalid city" in {
        val result = controllers.LocationController.findLocation("#DOESNTEXIST")(FakeRequest())
        status(result) must equalTo(OK)
        contentAsString(result) must contain("WHATEVER ERROR PAGE GIVES IN THIS CASE")
      }
    }
  }
  
}
