akka-cluster-demo
=================

Testing the Akka 2.4.0 feature ["akka.cluster.sharding.remember-entities"](http://doc.akka.io/docs/akka/current/scala/cluster-sharding.html#Remembering_Entities).


###Requirements:

SBT : sbt.version=0.13.8


###Re-creating the error

There are two main classes that can run that create a two node cluster.

You can run from SBT

sbt "runMain com.gvolpe.cluster.Dc1Cluster"

sbt "runMain com.gvolpe.cluster.Dc2Cluster"

Or use your favorite IDE and run Dc1Cluster & Dc2Cluster

Useful JMX Args...

```
-Dcom.sun.management.jmxremote.port=9998 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

There is a built in timer on DC1Cluster that will invokke the shutdown, you can comment this out and 
Fire up *jconsole* to connect to the leader (Dc1) on 'localhost:9998' from there you can see an mbean is exposed.

###What we are trying to achieve
A graceful removal of leader node from cluster, using Akka cluster 2.4, Akka persistence, Sharded Regios and remember entities.

When a node is asked to leave the cluster it should handoff all shard regions it has to other node in the cluster, if it is the
leader it also has to handover that responisibility to another node.  We would like the jvm to be shutdown once all handover
has completed and would not expect the new leader to have to remove the old node via the unresponsive route.

```
ERROR akka.remote.EndpointWriter - AssociationError [akka.tcp://KlasterSystem@127.0.0.1:2552] -> [akka.tcp://KlasterSystem@127.0.0.1:2551]: Error [Shut down address: akka.tcp://KlasterSystem@127.0.0.1:2551] [
akka.remote.ShutDownAssociation: Shut down address: akka.tcp://KlasterSystem@127.0.0.1:2551
Caused by: akka.remote.transport.Transport$InvalidAssociationException: The remote system terminated the association because it is shutting down.
```

Are we doing something wrong, is there a way to ensure the node leaves the cluster gracefully?

###Problems Observed:

####leaveClusterAndShutdown

This should be causing a graceful shutdown of the leader including handover of any Shared Regions on that node, to the other node in the cluster which will also become the Leader.

As part of the graceful shut down we want to shutdown the jvm once handover and cluster removal is finished!

We created an actor to handle the graceful shutdown, on receipt of a msg we send `region ! ShardRegion.GracefulShutdown`

We are seeing the node that requested to leave starts the handoff and it appears to complete succesfully. The other node in the cluster starts to become leader and we see
"Hand-over in progress" in the logs, although the old node is not removed gracefuly. I would expect the new leader to send a 'remove' message to other Node as per diagram
http://doc.akka.io/docs/akka/snapshot/common/cluster.html#State_Diagram_for_the_Member_States__akka_cluster_allow-weakly-up-members=off_ but this is not happening.

What happens is that the original node believes it has finished the handoff and vm exits, the new leader marks old node as UNREACHABLE after the heartbeat timeout
which then removes it from the cluster.  This not a very graceful removal from the cluster, and whilst that timeout happens messages will be backing up.


####cluster.leave(cluster.selfAddress)

We also tried just using the cluster.leave(cluster.selfAddress), this does not appear to do the handoff of shard regions and the status of the node stays in 'Exiting',
I added the 'registerOnMemberRemoved' callback but that seems to be called before leader handover is finished, so shutting down the vm at this point causes problems.
The new leader correctly removes the old node from the cluster so is slightly better than above, in terms of the cluster management that is. Shard handoff is still a problem!

