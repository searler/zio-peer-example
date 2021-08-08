package searler.zio_peer_example.ui

import searler.zio_peer_example.dto.{Component, UIDataFromController}
import zio.json.DeriveJsonDecoder

object Decoder {

  implicit val decoderComponent = DeriveJsonDecoder.gen[Component]
  implicit val decoderFrom = DeriveJsonDecoder.gen[UIDataFromController]

  def from(string: String): Either[String, UIDataFromController] = decoderFrom.decodeJson(string)


}
