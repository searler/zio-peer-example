package searler.zio_peer_example.dto


sealed trait UIDataFromController

case class Peers(peers: Set[Component]) extends UIDataFromController

sealed trait Initial

case object CONNECTED extends UIDataFromController with Initial

case object PERFORM extends UIDataFromController

sealed trait UIDataToController

case object REQUEST_INIT extends UIDataToController

case object PRESSED extends UIDataToController





