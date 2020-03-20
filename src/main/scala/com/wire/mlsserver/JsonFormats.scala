package com.wire.mlsserver

import Registry.ActionPerformed

//#json-formats
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)

  implicit val blobJsonFormat = jsonFormat2(Blob)
  implicit val blobsJsonFormat = jsonFormat1(Blobs)
}
//#json-formats
