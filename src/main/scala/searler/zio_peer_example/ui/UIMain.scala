package searler.zio_peer_example.ui

import searler.zio_peer.SingleConnector
import searler.zio_peer_example.dto.{CONNECTED,  Initial, REQUEST_INIT, UIData, UIDataFromController, UIDataToController}
import searler.zio_peer_example.json.Json
import searler.zio_tcp.TCP
import zio.duration._
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}
import zio.stream.{ZSink, ZStream}
import zio.{App, ExitCode, Hub, Promise, Queue, Schedule, URIO, ZHub, ZIO, console}

object UIMain extends App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {


    implicit val decoder: JsonDecoder[UIDataFromController] = DeriveJsonDecoder.gen[UIDataFromController]
    implicit val encoder: JsonEncoder[UIDataToController] = DeriveJsonEncoder.gen[UIDataToController]


    val program = for {
      shutdown <- Promise.make[Nothing, Unit]
      toController: ZHub[Any, Any, Nothing, Nothing, UIDataToController, String] <- Json.encodingSingle(Hub.bounded[String](10))
      fromController: ZHub[Any, Any, Nothing, Nothing, String, UIDataFromController] <- Json.decodingSingle(Hub.bounded[String](10))
     // _ <- ZStream.fromHub(toController).run(ZSink.foreach(s => console.putStrLn(s"toController $s"))).forkDaemon
    //  _ <- ZStream.fromHub(fromController).run(ZSink.foreach(s => console.putStrLn(s"fromController $s"))).forkDaemon
    _ <- ZStream.fromHub(fromController).filter(_ == CONNECTED).as(REQUEST_INIT).run(ZSink.fromHub(toController)).forkDaemon

      tracker <- Queue.bounded[Boolean](10)
      _ <- (tracker.filterOutput(_ == false).take *> shutdown.succeed(())).forkDaemon

      connector <- SingleConnector.strings[UIDataToController, Long](
        TCP.fromSocketClient(8886, "localhost", noDelay = true),
        tracker,
        toController,
        fromController.toQueue,
        reconnector = Schedule.spaced(1.second),
        Json.encode[Initial](Seq(CONNECTED))(DeriveJsonEncoder.gen[Initial])
      ).forkDaemon

      _ <- ZIO(UserInterface.create(toController.toQueue, fromController, shutdown))
      _ <- shutdown.await

    } yield ()

    program.exitCode

  }
}
