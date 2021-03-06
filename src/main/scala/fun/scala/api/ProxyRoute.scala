package fun.scala.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

import scala.concurrent.ExecutionContext

object ProxyRoute extends JsonSupport {
  implicit val system: ActorSystem = ActorSystem("outgoingServer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  lazy val proxy: Route = Route { context =>
    val request = context.request
    val requestWithMemberId = request.withHeaders(request.headers :+ RawHeader("x-member-id", "23232"))
    println("Opening connection to " + request.uri.authority.host.address)
    val flow = Http(system).outgoingConnection("localhost", 8080)
    Source.single(requestWithMemberId)
      .via(flow)
      .runWith(Sink.head)
      .flatMap(context.complete(_))
  }

}
