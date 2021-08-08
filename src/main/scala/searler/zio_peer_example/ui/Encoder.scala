package searler.zio_peer_example.ui

import searler.zio_peer_example.dto.{Component, UIDataToController}
import zio.json.{DeriveJsonEncoder, EncoderOps}

object Encoder {

  implicit val encoderComponent = DeriveJsonEncoder.gen[Component]
  implicit val encoderTo = DeriveJsonEncoder.gen[UIDataToController]

  def to(value: UIDataToController) = value.toJson

}
