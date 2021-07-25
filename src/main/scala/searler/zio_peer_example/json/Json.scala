package searler.zio_peer_example.json


import zio.{Hub, UIO, ZHub, ZIO}
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}

object Json {

  def encoding[A,T](hub:UIO[Hub[(A,String)]])(implicit decoder:JsonEncoder[T]) = hub.map(_
    .contramap((resp: (A, T)) => (resp._1, resp._2.toJson)))

  def decoding[A,T ](hub:UIO[Hub[(A,String)]])(implicit decoder:JsonDecoder[T]): ZIO[Any, Nothing, ZHub[Any, Any, Nothing, Nothing, (A, String), (A, T)]] = hub.map(_
    .map{case(host,json) => (host, json.fromJson[T])}
    .filterOutput(_._2.isRight)
    .map{case(host,errorOrResponse) => (host, errorOrResponse.toOption.get)})

  def encodingSingle[T](hub:UIO[Hub[String]])(implicit encoder:JsonEncoder[T]) = hub.map(_
    .contramap((resp: T) => resp.toJson))

  def decodingSingle[T ](hub:UIO[Hub[String]])(implicit decoder:JsonDecoder[T]): ZIO[Any, Nothing, ZHub[Any, Any, Nothing, Nothing, String,  T]] = hub.map(_
    .map{json =>  json.fromJson[T]}
    .filterOutput(_.isRight)
    .map{errorOrResponse => errorOrResponse.toOption.get})

  def encode[T](values:Seq[T]) (implicit encoder:JsonEncoder[T]): Seq[String] = values.map(_.toJson)

}
