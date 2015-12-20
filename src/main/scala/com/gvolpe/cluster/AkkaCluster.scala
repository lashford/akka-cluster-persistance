package com.gvolpe.cluster

import java.lang.management.ManagementFactory
import javax.management.ObjectName

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.sharding.ShardCoordinator.LeastShardAllocationStrategy
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.gvolpe.cluster.actors.EntityActor
import com.gvolpe.cluster.management.ClusterManagement
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaCluster(persistentId: String, port: Int) {

  println("AKKA CLUSTER CONSTRUCTOR")

  val actorSystem: ActorSystem = {
    val nodeConfig =
      s""" akka.remote.netty.tcp.port=${port}
       """.stripMargin
    val config = ConfigFactory.parseString(nodeConfig).withFallback(ConfigFactory.load())
    val system = ActorSystem("KlasterSystem", config)

    ClusterSharding(system).start(
      typeName = "Klaster",
      entityProps = EntityActor.props(persistentId),
      settings = ClusterShardingSettings(system),
      extractEntityId = EntityActor.idExtractor,
      extractShardId = EntityActor.shardResolver,
      allocationStrategy = new LeastShardAllocationStrategy(2, 2),
      handOffStopMessage = Stop
    )

    registerClusterManagementBean(system)

    system
  }

  def registerClusterManagementBean(system: ActorSystem): Unit = {
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()
    val clusterMgmtMBeanName = new ObjectName("KlasterDemo:name=ClusterManagement")
    mbeanServer.registerMBean(new ClusterManagement(system, port), clusterMgmtMBeanName)
  }

  def actor(props: Props) = actorSystem.actorOf(props)

  def awaitTermination = Await.result(actorSystem.whenTerminated, 1000 seconds)

}