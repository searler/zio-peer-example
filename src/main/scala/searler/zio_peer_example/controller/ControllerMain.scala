package searler.zio_peer_example.controller

import searler.zio_peer.{ALL, Acceptor, AcceptorTracker, Routing, Single}
import searler.zio_peer_example.Driver.{Request, Response}
import searler.zio_peer_example.dto.{CONNECTED, Component, Decoder, Encoder, Node, Peers, REQUEST_INIT, UI, UIDataFromController, UIDataToController}
import searler.zio_peer_example.json.Json
import searler.zio_tcp.TCP
import zio._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder}
import zio.stream.{Transducer, ZSink, ZStream, ZTransducer}

object ControllerMain extends App{
  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {




    val program = for {

      tracker <- AcceptorTracker.dropOld[Component]
      _ <- tracker.changes.run(ZSink.foreach(keys => console.putStrLn(keys.toString()))).fork

      outgoingHub <-
        Json.encoding(Encoder.from)(ZHub.sliding[(Routing[Component], String)](20))
      _ <- tracker.changes.map(p => ALL -> Peers(p-UI)).run(ZSink.fromHub(outgoingHub)).fork


      incomingHub: ZHub[Any, Any, Nothing, Nothing, (Component, String), (Component, UIDataToController)] <- Json.decoding(Decoder.to)(ZHub.sliding[(Component, String)](20))




      // _ <- ZStream.fromHub(requestHub).map(process).run(ZSink.fromHub(responseHub)).fork
      _ <- ZStream.fromHub(outgoingHub).run(ZSink.foreach(s => console.putStrLn(s"outgoingHub $s"))).forkDaemon
      _ <- ZStream.fromHub(incomingHub).run(ZSink.foreach(s => console.putStrLn(s"incomingHub $s"))).forkDaemon


      nodeMap <- Node(Map("localhost" -> 0, "golem" -> 1))

      _ <- Acceptor.strings[Component, UIDataFromController ](TCP.fromSocketServer(8886, noDelay = true),
        20,
        Node.mapped(nodeMap),
        tracker,
        outgoingHub,
        incomingHub.toQueue
      )

    } yield ()

    program.exitCode

  }
}
