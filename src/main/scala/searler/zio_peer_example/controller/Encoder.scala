package searler.zio_peer_example.controller

import searler.zio_peer_example.dto.{Component, FromController, UIDataFromController}
import zio.json.{DeriveJsonEncoder, EncoderOps}

object Encoder {

  implicit val encoderComponent = DeriveJsonEncoder.gen[Component]
  implicit val encoderToUI = DeriveJsonEncoder.gen[UIDataFromController]
  implicit val encoderToOther = DeriveJsonEncoder.gen[FromController]


  def toUI(value: UIDataFromController) = value.toJson
  def toOther(value: FromController) = value.toJson
}
