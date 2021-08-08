package searler.zio_peer_example.controller

import searler.zio_peer_example.dto.{Component, UIDataFromController}
import zio.json.{DeriveJsonEncoder, EncoderOps}

object Encoder {

  implicit val encoderComponent = DeriveJsonEncoder.gen[Component]
  implicit val encoderFrom = DeriveJsonEncoder.gen[UIDataFromController]

  def from(value: UIDataFromController) = value.toJson
}
