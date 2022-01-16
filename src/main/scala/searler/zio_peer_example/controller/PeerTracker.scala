package searler.zio_peer_example.controller

import io.github.searler.zio_peer.AcceptorTracker
import searler.zio_peer_example.dto.{Component, Node, Peers, UI}
import zio.UIO
import zio.stream.UStream

class PeerTracker(selfNode: Node, tracker: AcceptorTracker[Component]) {

  private def create(nodes: Set[Component]) = Peers(nodes - UI + selfNode)

  def get: UIO[Peers] = tracker.get.map(create)

  def changes: UStream[Peers] = tracker.changes.map(create).changes
}

object PeerTracker {
  def apply(nodes: Map[String, Int], tracker: AcceptorTracker[Component]) = for {
    selfNode <- Node.self(nodes)
  } yield new PeerTracker(selfNode, tracker)


}
