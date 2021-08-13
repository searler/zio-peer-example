package searler.zio_peer_example.controller

import searler.zio_peer._
import searler.zio_peer_example.dto._
import searler.zio_peer_example.json.Json
import searler.zio_tcp.TCP
import zio._
import zio.stream.{ZSink, ZStream}

object ControllerMain extends App {

  type  PHub[A,B] = ZHub[Any, Any, Nothing, Nothing, A, B]

  val NOT_UI: AllBut[Component] = AllBut(UI)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val NODES = Map("localhost" -> 0, "golem" -> 1, "192.168.1.55" -> 2)

    val program = for {

      tracker <- AcceptorTracker.dropOld[Component]

      peerTracker <- PeerTracker(NODES, tracker)

      outgoingHub: PHub[ (Routing[Component], UIDataFromController), (Routing[Component], String)] <-
        Json.encoding(Encoder.from)(ZHub.sliding[(Routing[Component], String)](8))

      incomingHub: PHub[ (Component, String), (Component, UIDataToController)] <- Json.decoding(Decoder.to)(ZHub.sliding[(Component, String)](20))

      _ <- peerTracker.changes.map(p => ALL -> p).run(ZSink.fromHub(outgoingHub)).forkDaemon


      handlers: Function[(Component, UIDataToController), ZIO[Any, Nothing, (Routing[Component], UIDataFromController)]] = {
        case (src, REQUEST_INIT) => peerTracker.get.map(p => Single(src) -> p)
        case (UI, PRESSED) => ZIO.succeed(NOT_UI -> PERFORM)
        case (Node(_), PRESSED) => ZIO.succeed(IGNORE -> PERFORM)
      }

      _ <- ZStream.fromHub(incomingHub).mapMParUnordered(2)(handlers).run(ZSink.fromHub(outgoingHub)).forkDaemon

      _ <- ZStream.fromHub(outgoingHub).run(ZSink.foreach(s => console.putStrLn(s"outgoingHub $s"))).forkDaemon
      _ <- ZStream.fromHub(incomingHub).run(ZSink.foreach(s => console.putStrLn(s"incomingHub $s"))).forkDaemon

      nodeMap <- Node(NODES)

      _ <- Acceptor.strings[Component, UIDataFromController](TCP.fromSocketServer(8886, noDelay = true),
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
