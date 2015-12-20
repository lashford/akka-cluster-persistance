package com.gvolpe.cluster.management

import akka.actor.ActorSystem
import akka.cluster.{MemberStatus, Cluster}
import com.gvolpe.cluster.actors.GracefulShutdownActor

import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.duration._

class ClusterManagement(system: ActorSystem, port: Int) extends ClusterManagementMBean {

  override def leaveClusterAndShutdown(): Unit = {
    system.log.info(s"INVOKING MBEAN ${system.name}")

    // This should perform a graceful shutdown, and once Shard regions are handed over to the other Node, this node should leave the cluster.
    val shutdownActor = system.actorOf(GracefulShutdownActor.props)
    shutdownActor ! GracefulShutdownActor.LeaveAndShutdownNode

  }
}
