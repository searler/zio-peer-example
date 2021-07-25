package searler.zio_peer_example.dto

import searler.zio_tcp.Address
import zio.ZIO
import zio.blocking.Blocking

import java.net.{InetAddress, InetSocketAddress, SocketAddress}

case class Node(number: Int)

object Node {
  def apply(map: Map[String, Int]): ZIO[Blocking, Nothing, Map[InetAddress, Node]] =
    Address.byName(map.keySet).map(_.map(p => p._2 -> Node(map(p._1))).toMap)

  def mapped(mapping: Map[InetAddress, Node])(sa: SocketAddress): Option[Node] =
    mapping.get(sa.asInstanceOf[InetSocketAddress].getAddress)

}
