akka-cluster-demo
=================

Testing the Akka 2.4.0 feature ["akka.cluster.sharding.remember-entities"](http://doc.akka.io/docs/akka/current/scala/cluster-sharding.html#Remembering_Entities).


###Requirements:

SBT : sbt.version=0.13.8


###Re-creating the error

There are two main classes that can run that create a two node cluster.

You can run from SBT

sbt "runMain com.gvolpe.cluster.Dc1Cluster"

sbt "runMain com.gvolpe.cluster.Dc1Cluster"

Or use your favorite IDE and run Dc1Cluster & Dc2Cluster

Useful JMX Args...

```
-Dcom.sun.management.jmxremote.port=9998 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

There is a built in timer on DC1Cluster that will invokke the shutdown, you can comment this out and 
Fire up *jconsole* to connect to the leader (Dc1) on 'localhost:9998' from there you can see an mbean is exposed.

###Problems Observed:

####leaveClusterAndShutdown

This should be causing a graceful shutdown of the leader including handover of any Shared Regions on that node, to the other node in the cluster which will also become the Leader.

As part of the graceful shut down we want to shutdown the jvm once handover and cluster removal is finished!

We created an actor to handle the graceful shutdown, on receipt of a msg we send `region ! ShardRegion.GracefulShutdown`

We are seeing the node that requested to leave starts the handoff and the other node in the cluster becomes leader.  The problem we have is
identifying when the handoff is complete, i would expect the new leader to send a 'remove' message to other Node as per diagram
http://doc.akka.io/docs/akka/snapshot/common/cluster.html#State_Diagram_for_the_Member_States__akka_cluster_allow-weakly-up-members=off_ but this is not happening.

What happens is that the original node believes it has finished the handoff and vm exits, the new leader marks old node as UNREACHABLE after the heartbeat timeout
which then removes it from the cluster.  This not a very graceful removal from the cluster, and whilst that timeout happens messages will be backing up.


####cluster.leave(cluster.selfAddress)

We also tried just using the cluster.leave(cluster.selfAddress), this does not appear to do the handoff of shard regions and the status of the node stays in 'Exiting',
I added the 'registerOnMemberRemoved' callback but that seems to be called before leader handover is finished, so shutting down the vm at this point causes problems.
The new leader correctly removes the old node from the cluster so is slightly better than above, in terms of the cluster management that is. Shard handoff is still a problem!

