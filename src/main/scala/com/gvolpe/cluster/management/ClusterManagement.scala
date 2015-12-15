package com.gvolpe.cluster.management

import akka.actor.ActorSystem
import akka.cluster.{MemberStatus, Cluster}
import com.gvolpe.cluster.actors.GracefulShutdownActor

import scala.concurrent.Await
import scala.util.Try
import scala.concurrent.duration._

class ClusterManagement(system: ActorSystem, port: Int) extends ClusterManagementMBean {

  override def leaveClusterAndShutdown(): Unit = {
    println(s"INVOKING MBEAN ${system.name}")

    // This should perform a graceful shutdown, and once Shard regions are handed over to the other Node, this node should leave the cluster.
    val shutdownActor = system.actorOf(GracefulShutdownActor.props)
    shutdownActor ! GracefulShutdownActor.LeaveAndShutdownNode

    // This is the equivilent of just using the existing Akka Mbean, but you don't have to pass the URI
//    cluster.leave(cluster.selfAddress)
  }


  private val cluster:Cluster = {
    val cluster:Cluster = Cluster(system)
    cluster.registerOnMemberRemoved {


      println(s">>>>>>> ${cluster.state.members.find(_.address == cluster.selfAddress)}")

      // bit of hack to wait for status to change.
//      while(true){
//        println(s">>>>>>> ${cluster.state.members.find(_.address == cluster.selfAddress)}")
//        if(cluster.state.members.find(_.address == cluster.selfAddress).get.status == MemberStatus.removed){
//
//          // Shutdown the JVM with failover ((@see http://doc.akka.io/docs/akka/snapshot/scala/cluster-usage.html#How_To_Cleanup_when_Member_is_Removed))
//          system.registerOnTermination(System.exit(0))
//          system.terminate()
//          new Thread {
//            override def run(): Unit = {
//              if (Try(Await.ready(system.whenTerminated, 10.seconds)).isFailure)
//                System.exit(-1)
//            }
//          }.start()
//
//        }
//        Thread.sleep(3000)
//      }
    }
    cluster
  }

}
