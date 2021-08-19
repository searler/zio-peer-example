package searler.zio_peer_example.json


import zio.{Hub, UIO, ZHub, ZIO}
import zio.json.{DecoderOps, DeriveJsonDecoder, DeriveJsonEncoder, EncoderOps, JsonDecoder, JsonEncoder}

object Json {

  def encodingWrap[A,T](encoder:T=>String)(hub:UIO[Hub[(A,String)]]) = hub.map(_
    .contramap((resp: (A, T)) => (resp._1, encoder(resp._2))))

  def encoding[A,T](encoder:T=>String)(hub:Hub[(A,String)]) = hub.contramap((resp: (A, T)) => (resp._1, encoder(resp._2)))


  def decoding[A,T ](decoder:String =>Either[String,T])(hub:UIO[Hub[(A,String)]]): ZIO[Any, Nothing, ZHub[Any, Any, Nothing, Nothing, (A, String), (A, T)]] = hub.map(_
    .map{case(host,json) => (host, decoder(json))}
    .filterOutput(_._2.isRight)
    .map{case(host,errorOrResponse) => (host, errorOrResponse.toOption.get)})

  def encodingSingle[T](encoder:T=>String)(hub:UIO[Hub[String]]) = hub.map(_
    .contramap((resp: T) => encoder(resp)))

  def decodingSingle[T ](decoder:String =>Either[String,T])(hub:UIO[Hub[String]]): ZIO[Any, Nothing, ZHub[Any, Any, Nothing, Nothing, String,  T]] = hub.map(_
    .map{json =>  decoder(json)}
    .filterOutput(_.isRight)
    .map{errorOrResponse => errorOrResponse.toOption.get})

  def encode[T](values:Seq[T]) (implicit encoder:JsonEncoder[T]): Seq[String] = values.map(_.toJson)

}
