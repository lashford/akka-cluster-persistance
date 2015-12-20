package com.gvolpe.cluster.actors

import akka.actor.{ActorLogging, Terminated, Props, Actor}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ShardRegion}
import akka.event.LoggingReceive
import com.gvolpe.cluster.actors.GracefulShutdownActor.LeaveAndShutdownNode
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object GracefulShutdownActor {
  case object LeaveAndShutdownNode
  def props = Props[GracefulShutdownActor]
}

class GracefulShutdownActor extends Actor with ActorLogging  {

  import context.dispatcher
  val system = context.system
  val cluster = Cluster(system)
  val region = ClusterSharding(system).shardRegion(EntityActor.shardName)

  def receive = LoggingReceive {
    case LeaveAndShutdownNode =>
      log.info(">>>>>>>>>>>> LeaveAndShutdownNode")
      context.watch(region)
      region ! ShardRegion.GracefulShutdown

    case Terminated(`region`) =>
      log.info(">>>>>>>>>>>> Terminating node")
      cluster.registerOnMemberRemoved {
        // Let singletons hand over gracefully before stopping the system
        system.scheduler.scheduleOnce(3.seconds, self, "stop-system")
      }
      cluster.leave(cluster.selfAddress)

    case "stop-system" =>
      log.info(">>>>>>>>>>>> stop system")
      // Shutdown the JVM with failover ((@see http://doc.akka.io/docs/akka/snapshot/scala/cluster-usage.html#How_To_Cleanup_when_Member_is_Removed))
      system.registerOnTermination(System.exit(0))
      system.terminate()
      new Thread {
        override def run(): Unit = {
          if (Try(Await.ready(system.whenTerminated, 10.seconds)).isFailure)
            System.exit(-1)
        }
      }.start()
  }

}