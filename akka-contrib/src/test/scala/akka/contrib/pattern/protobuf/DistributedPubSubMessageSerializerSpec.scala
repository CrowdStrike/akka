/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package akka.contrib.pattern.protobuf

import akka.actor.{ ExtendedActorSystem, Address }
import akka.testkit.AkkaSpec
import akka.contrib.pattern.DistributedPubSubMediator._
import akka.contrib.pattern.DistributedPubSubMediator.Internal._
import akka.actor.Props

@org.junit.runner.RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DistributedPubSubMessageSerializerSpec extends AkkaSpec {

  val serializer = new DistributedPubSubMessageSerializer(system.asInstanceOf[ExtendedActorSystem])

  def checkSerialization(obj: AnyRef): Unit = {
    val blob = serializer.toBinary(obj)
    val ref = serializer.fromBinary(blob, obj.getClass)
    ref should be(obj)
  }

  " DistributedPubSubMessages" must {

    "be serializable" in {
      val address1 = Address("akka.tcp", "system", "some.host.org", 4711)
      val address2 = Address("akka.tcp", "system", "other.host.org", 4711)
      val address3 = Address("akka.tcp", "system", "some.host.org", 4712)
      val u1 = system.actorOf(Props.empty, "u1")
      val u2 = system.actorOf(Props.empty, "u2")
      val u3 = system.actorOf(Props.empty, "u3")
      val u4 = system.actorOf(Props.empty, "u4")
      checkSerialization(Status(Map(address1 -> 3, address2 -> 17, address3 -> 5)))
      checkSerialization(Delta(List(
        Bucket(address1, 3, Map("/user/u1" -> ValueHolder(2, Some(u1)), "/user/u2" -> ValueHolder(3, Some(u2)))),
        Bucket(address2, 17, Map("/user/u3" -> ValueHolder(17, Some(u3)))),
        Bucket(address3, 5, Map("/user/u4" -> ValueHolder(4, Some(u4)), "/user/u5" -> ValueHolder(5, None))))))
      checkSerialization(Send("/user/u3", "hello", localAffinity = true))
      checkSerialization(SendToAll("/user/u3", "hello", allButSelf = true))
      checkSerialization(Publish("mytopic", "hello"))
    }
  }
}
