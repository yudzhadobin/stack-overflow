import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Context {
  implicit val system: ActorSystem = ActorSystem("parent-system")

  implicit val materializer: ActorMaterializer = ActorMaterializer()

}
