package searler.zio_peer_example.swing

import zio.stream.{UStream, ZStream}
import zio.{Enqueue, Hub, Promise, Ref, RefM, Runtime, ZIO}

import javax.swing.SwingUtilities

object ZIOSwing {
  val runtime = Runtime.default

  def triggerShutdown(shutdown: Promise[Nothing, Unit]): Unit = runtime.unsafeRun(shutdown.succeed(()))

  def createOutgoing[S](outgoing: Enqueue[S]): S => Unit = cmd => runtime.unsafeRun(outgoing.offer(cmd))

  def get[T](ref: Ref[T]): T = runtime.unsafeRun(ref.get)

  def set[T](ref: Ref[T], a: T): Unit = runtime.unsafeRun(ref.set(a))

  def getM[T](ref: RefM[T]): T = runtime.unsafeRun(ref.get)

  def createIncoming[S](incoming: UStream[S], target: S => Unit, f: S => Boolean = (_: S) => true) =
    runtime.unsafeRun(incoming.filter(f).
      foreach(data => ZIO.effectTotal(SwingUtilities.invokeLater(() => target(data)))).forkDaemon)



}
