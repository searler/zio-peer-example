package searler.zio_peer_example

import searler.zio_peer_example.ui.UserInterface

import java.util.concurrent.CountDownLatch
import javax.swing.SwingUtilities

object UIOnlyMain extends App {

  val latch = new CountDownLatch(1)

  SwingUtilities.invokeLater(() => new UserInterface(println _, latch.countDown()))

  latch.await()

}
