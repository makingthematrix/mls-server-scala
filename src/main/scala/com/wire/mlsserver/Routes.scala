package com.wire.mlsserver

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{PathMatcher, Route}
import com.wire.mlsserver.Registry.{ActionPerformed, AppendBlob, GetBlobs}

import scala.concurrent.Future

class Routes(registry: ActorRef[Registry.Command])(implicit val system: ActorSystem[_]) {
  // If ask takes more time than this to complete the request is failed
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getBlobs(id: String): Future[Blobs] = registry.ask(GetBlobs(id, _))
  def appendBlob(id: String, blob: Blob): Future[ActionPerformed] = registry.ask(AppendBlob(id, blob, _))

  val routes: Route = pathPrefix("groups") {
    pathPrefix(PathMatcher("""\S.+""".r)) { groupId =>
      pathPrefix("blobs") {
        parameterMap { _ =>
          concat(
            get { complete(getBlobs(groupId)) },
            post {
              entity(as[Blob]) { blob =>
                onSuccess(appendBlob(groupId, blob)) { performed => complete((StatusCodes.OK, performed)) }
              }
            }
          )
        }
      }
    }
  }

}
