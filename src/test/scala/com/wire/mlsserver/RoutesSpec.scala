package com.wire.mlsserver

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import akka.actor.typed.scaladsl.adapter._

class RoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem = testKit.system.toClassic

  val registry = testKit.spawn(Registry())
  lazy val routes = new Routes(registry).routes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  "UserRoutes" should {
    "return no blobs if no present (GET /groups/group_name/blobs)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/groups/group_name/blobs")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"blobs":[]}""")
      }
    }

    "be able to add users (POST /groups/group_name/blobs)" in {
      val blob = Blob(1, "xyz")
      val blobEntity = Marshal(blob).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/groups/group_name/blobs").withEntity(blobEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"A new blob group create with id group_name"}""")
      }
    }

  }

}
