package searler.zio_peer_example.dto

import searler.zio_tcp.Address
import zio.ZIO
import zio.blocking.Blocking

import java.net.{ InetSocketAddress, SocketAddress,InetAddress}


sealed trait Component
case object UI extends Component
case class Node(number: Int) extends Component

object Node {
  val LOOPBACK = InetAddress.getLoopbackAddress
  def apply(map: Map[String, Int]): ZIO[Blocking, Nothing, Map[InetAddress, Node]] = {
    Address.byName(map.keySet).map(_.map(p => p._2 -> Node(map(p._1))).toMap)
  }

  def mapped(mapping: Map[InetAddress, Component])(sa: SocketAddress): Option[Component] =
     sa.asInstanceOf[InetSocketAddress].getAddress match {
       case LOOPBACK => Option(UI)
       case addr => mapping.get(addr)
  }

  def self(map: Map[String, Int]) = (Address.localhost).map(addr => Node(map(addr.getHostName)))

}
