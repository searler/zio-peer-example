package searler.zio_peer_example.controller

import searler.zio_peer_example.dto.{Component, UIDataToController}
import zio.json.DeriveJsonDecoder

object Decoder {

  implicit val decoderComponent = DeriveJsonDecoder.gen[Component]

  implicit val decoderTo = DeriveJsonDecoder.gen[UIDataToController]

  def to(string: String) = decoderTo.decodeJson(string)
}
