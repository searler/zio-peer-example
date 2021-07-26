package searler.zio_peer_example.ui

import searler.zio_peer_example.dto.{PRESSED, UIDataFromController, UIDataToController}
import zio.stream.UStream
import zio.{Enqueue, Promise}

import java.awt.BorderLayout
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing._


class UserInterface(val outgoing: UIDataToController => Unit, shutdown: => Unit) {

  private val frame = new JFrame

  private val button = new JButton("Press")
  private val text = new JLabel("LABEL")


  frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  frame.setBounds(100, 100, 200, 200)

  frame.add(button, BorderLayout.SOUTH)
  frame.add(text)

  frame.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) = {
      shutdown;
      frame.dispose()
    }
  })


  button.addActionListener(_ => outgoing(PRESSED))


  frame.setTitle("UI")

  frame.pack()

  frame.validate()

  frame.setVisible(true)

  def acceptor(incoming: UIDataFromController) = text.setText(incoming.toString)

}

object UserInterface {

  import searler.zio_peer_example.swing.ZIOSwing._

  def create(toController: Enqueue[UIDataToController], fromController: UStream[UIDataFromController], shutdown: Promise[Nothing, Unit]): Unit = {

    SwingUtilities.invokeLater(() => {
      val ui = new UserInterface(createOutgoing[UIDataToController](toController),
        triggerShutdown(shutdown))

      createIncoming(fromController, ui.acceptor)
    })

  }
}
