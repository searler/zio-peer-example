package searler.zio_peer_example.swing

import zio.stream.ZStream
import zio.{Hub, Promise, Ref, RefM, Runtime, ZIO}

import javax.swing.SwingUtilities

object ZIOSwing {
  val runtime = Runtime.default

  def triggerShutdown(shutdown: Promise[Nothing, Unit]): Unit = runtime.unsafeRun(shutdown.succeed(()))

  def createOutgoing[S](upstream: Hub[S]): S => Unit = cmd => runtime.unsafeRun(upstream.publish(cmd))

  def get[T](ref: Ref[T]): T = runtime.unsafeRun(ref.get)

  def set[T](ref: Ref[T], a: T): Unit = runtime.unsafeRun(ref.set(a))

  def getM[T](ref: RefM[T]): T = runtime.unsafeRun(ref.get)

  def createIncoming[S](downStream: Hub[S], target: S => Unit, f: S => Boolean = (_: S) => true) =
    runtime.unsafeRun(ZStream.fromHub(downStream).filter(f).
      foreach(data => ZIO.effectTotal(SwingUtilities.invokeLater(() => target(data)))).forkDaemon)



}
