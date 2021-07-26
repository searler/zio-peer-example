package searler.zio_peer_example.dto

import zio.json.DeriveJsonDecoder

object Decoder {

  implicit val decoderComponent = DeriveJsonDecoder.gen[Component]
  implicit val decoderFrom = DeriveJsonDecoder.gen[UIDataFromController]
  implicit val decoderTo = DeriveJsonDecoder.gen[UIDataToController]

  def from(string: String): Either[String, UIDataFromController] = decoderFrom.decodeJson(string)

  def to(string: String) = decoderTo.decodeJson(string)

}
