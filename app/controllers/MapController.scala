package controllers

import scala.util.{Try, Success, Failure} 
import play.api._
import play.api.mvc._
import models._
import models.Location
import globals.Globals.defaultDB

import java.net.URLEncoder
import play.api.libs.ws.WS
import play.api.Play.current
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

object MapController extends Controller {
  def showMap(address: String, city: String) = Action {
    Ok(views.html.locations.displayMap(address, city))
  }
}