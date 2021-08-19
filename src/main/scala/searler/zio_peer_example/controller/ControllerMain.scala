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

      outgoingHub  <- ZHub.sliding[(Routing[Component], String)](8)

      toUIHub: PHub[ (Routing[Component], UIDataFromController), (Routing[Component], String)] =
        Json.encoding(Encoder.toUI)(outgoingHub)

      toOtherHub: PHub[ (Routing[Component], FromController), (Routing[Component], String)] =
        Json.encoding(Encoder.toOther)(outgoingHub)

      incomingHub: PHub[ (Component, String), (Component, UIDataToController)] <- Json.decoding(Decoder.to)(ZHub.sliding[(Component, String)](20))

      _ <- peerTracker.changes.map(p => ALL -> p).run(ZSink.fromHub(toUIHub)).forkDaemon

      uihandlers: Function[(Component, UIDataToController), ZIO[Any, Nothing, (Routing[Component], UIDataFromController)]] = {
        case (src, REQUEST_INIT) => peerTracker.get.map(p => Single(src) -> p)
        case (_, PRESSED) => ZIO.succeed(IGNORE -> null)
      }


      handlers: Function[(Component, UIDataToController), ZIO[Any, Nothing, (Routing[Component], FromController)]] = {
        case (UI, PRESSED) => ZIO.succeed(NOT_UI -> PERFORM)
        case (Node(_), PRESSED) => ZIO.succeed(IGNORE -> _)
        case (_, REQUEST_INIT) => ZIO.succeed(IGNORE -> _)
      }

      _ <- ZStream.fromHub(incomingHub).mapMParUnordered(2)(uihandlers).run(ZSink.fromHub(toUIHub)).forkDaemon
      _ <- ZStream.fromHub(incomingHub).mapMParUnordered(2)(handlers).run(ZSink.fromHub(toOtherHub)).forkDaemon

      _ <- ZStream.fromHub(toUIHub).run(ZSink.foreach(s => console.putStrLn(s"toUIHub $s"))).forkDaemon
      _ <- ZStream.fromHub(toOtherHub).run(ZSink.foreach(s => console.putStrLn(s"toOtherHub $s"))).forkDaemon
      _ <- ZStream.fromHub(incomingHub).run(ZSink.foreach(s => console.putStrLn(s"incomingHub $s"))).forkDaemon

      nodeMap <- Node(NODES)

      _ <- Acceptor.strings[Component](TCP.fromSocketServer(8886, noDelay = true),
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
