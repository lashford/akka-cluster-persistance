package com.gvolpe.cluster.management

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.gvolpe.cluster.actors.GracefulShutdownActor

import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.duration._

class ClusterManagement(system: ActorSystem, port: Int) extends ClusterManagementMBean {

  override def leaveClusterAndShutdown(): Unit = {
    println(s"INVOKING MBEAN ${system.name}")

    val shutdownActor = system.actorOf(GracefulShutdownActor.props)
    shutdownActor ! GracefulShutdownActor.LeaveAndShutdownNode
//    cluster.leave(cluster.selfAddress)
  }


//  private val cluster:Cluster = {
//    val cluster:Cluster = Cluster(system)
//    cluster.registerOnMemberRemoved {
//      system.registerOnTermination(System.exit(0))
//      system.terminate()
//      new Thread {
//        override def run(): Unit = {
//          if (Try(Await.ready(system.whenTerminated, 10.seconds)).isFailure)
//            System.exit(-1)
//        }
//      }.start()
//    }
//    cluster
//  }

}
