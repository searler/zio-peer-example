package searler.zio_peer_example.dto

import zio.json.{DeriveJsonEncoder, EncoderOps}

object Encoder {

  implicit val encoderComponent = DeriveJsonEncoder.gen[Component]
  implicit val encoderFrom = DeriveJsonEncoder.gen[UIDataFromController]
  implicit val encoderTo = DeriveJsonEncoder.gen[UIDataToController]

  def from(value: UIDataFromController) = value.toJson

  def to(value: UIDataToController) = value.toJson

}
