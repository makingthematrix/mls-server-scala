package com.wire.mlsserver

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

case class Blob(index: Int, content: String)
case class Blobs(blobs: Seq[Blob])

object Registry {
  val EmptyBlobs = Blobs(Nil)

  sealed trait Command
  case class GetBlobs(id: String, replyTo: ActorRef[Blobs]) extends Command
  case class AppendBlob(id: String, blob: Blob, replyTo: ActorRef[ActionPerformed]) extends Command

  case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry()

  private def registry(blobMap: Map[String, Blobs] = Map.empty): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetBlobs(id, replyTo) =>
        replyTo ! blobMap.getOrElse(id, EmptyBlobs)
        Behaviors.same
      case AppendBlob(id, blob, replyTo) =>
        val (newGroups, answer) = blobMap.get(id) match {
          case Some(blobs) =>
            (blobMap ++ Map(id -> Blobs(blobs.blobs ++ Seq(blob))), s"A new blob added to $id")
          case None =>
            (blobMap ++ Map(id -> Blobs(Seq(blob))), s"A new blob group create with id $id")
        }
        replyTo ! ActionPerformed(answer)
        registry(newGroups)
    }
}
