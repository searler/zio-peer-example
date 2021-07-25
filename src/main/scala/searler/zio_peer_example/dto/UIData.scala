package searler.zio_peer_example.dto



sealed trait UIData

sealed trait UIDataFromController extends  UIData
sealed trait UIDataToController extends  UIData

sealed trait Initial

case object CONNECTED extends  UIDataFromController with Initial
case object REQUEST_INIT extends UIDataToController
case object PRESSED extends UIDataToController





