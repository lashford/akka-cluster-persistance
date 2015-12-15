package com.gvolpe.cluster.actors

import akka.actor.{Terminated, Props, Actor}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ShardRegion}
import akka.event.LoggingReceive
import com.gvolpe.cluster.actors.GracefulShutdownActor.LeaveAndShutdownNode

object GracefulShutdownActor {
  case object LeaveAndShutdownNode
  def props = Props[GracefulShutdownActor]
}

class GracefulShutdownActor extends Actor {

  val cluster = Cluster(context.system)
  val region = ClusterSharding(context.system).shardRegion(EntityActor.shardName)

  def receive = LoggingReceive {
    case LeaveAndShutdownNode =>
      context.watch(region)
      println(">>>>>>>>>>>> Leaving node")
      region ! ShardRegion.GracefulShutdown
    case Terminated(`region`) =>
      println(">>>>>>>>>>>> Terminating node")
      cluster.registerOnMemberRemoved(context.system.terminate())
      cluster.leave(cluster.selfAddress)
  }

}
