package com.gvolpe.cluster

import com.gvolpe.cluster.actors.MessageGenerator
import com.gvolpe.cluster.management.ClusterManagement
import scala.concurrent.duration._

object Dc1Cluster extends App {

  val cluster = new AkkaCluster("dc1", 2551)

  cluster.actor(MessageGenerator.props)

  import cluster.actorSystem.dispatcher

  cluster.actorSystem.scheduler.scheduleOnce(30.seconds) {
    cluster.actorSystem.log.info("Cluster shutdown triggered on DC1")
    val manager = new ClusterManagement(cluster.actorSystem, 2551)
    manager.leaveClusterAndShutdown()
  }

  cluster.awaitTermination

}
