package com.gvolpe.cluster

import com.gvolpe.cluster.management.ClusterManagement
import scala.concurrent.duration._

object Dc2Cluster extends App {

  val cluster = new AkkaCluster("dc2", 2552)

//  import cluster.actorSystem.dispatcher
//
//  cluster.actorSystem.scheduler.scheduleOnce(30.seconds) {
//    cluster.actorSystem.log.info("Cluster shutdown triggered on DC2")
//    val manager = new ClusterManagement(cluster.actorSystem, 2552)
//    manager.leaveClusterAndShutdown()
//  }

  cluster.awaitTermination

}
