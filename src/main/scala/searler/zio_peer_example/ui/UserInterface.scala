package searler.zio_peer_example.ui

import zio.stream.ZStream
import zio.{Hub, Promise, Runtime, ZIO}

import java.awt.BorderLayout
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing._


class UserInterface(val outgoing: String => Unit, shutdown: => Unit) {


  private val frame = new JFrame

  private val button = new JButton("Press")
  private val text = new JLabel("LABEL")


  frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  frame.setBounds(100, 100, 200, 200)

  frame.add(button, BorderLayout.SOUTH)
  frame.add(text)

  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) = {
      shutdown; frame.dispose()
    }
  })


  button.addActionListener(_ => outgoing("pressed"))


  frame.setTitle("UI")

  frame.pack()

  frame.validate()

  frame.setVisible(true)

  def acceptor(incoming: String) = text.setText(incoming.toString)

}

object UserInterface {
 val runtime = Runtime.default

  def create(upstream: Hub[String], downStream: Hub[String], shutdown: Promise[Nothing, Unit]): Unit = {

    val pusher: String => Unit = cmd => runtime.unsafeRun(upstream.publish(cmd))

    def triggerShutdown: Unit = runtime.unsafeRun(shutdown.succeed(()))

    SwingUtilities.invokeLater(() => {
      val ui = new UserInterface(pusher, triggerShutdown)
      runtime.unsafeRun(ZStream.fromHub(downStream).foreach(data => ZIO.effectTotal(SwingUtilities.invokeLater(() => ui.acceptor(data)))).forkDaemon)
    })

  }
}
