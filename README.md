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

Or use your favorite editor and run Dc1Cluster  & Dc2Cluster

JMX Args...

```
-Dcom.sun.management.jmxremote.port=9998 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
```

There is a built in timer on DC1Cluster that will invokke the shutdown, you can comment this out and 
Fire up *jconsole* to connect to the leader (usually Dc1) 'localhost:9998'

From here select *mbeans* > *akka* > *cluster* > *operations*

then call 'Leave("akka.tcp://KlasterSystem@127.0.0.1:2551")'

This will cause the leader to handover the Shared Regions that it has and pass them to the another Node, this other node also becomes the new leader.

Handover is successful but we also want to shutdown the jvm once handover and cluster removal is finished!

Problems Observed:

+Problem seems to be the *registerOnMemberRemoved* event is called straightaway even though Shard Region handoff has not completed
+Cluster status of the node that was removed never gets passed 'Exiting'
+New leader node never sends 'remove' message to other Node as per diagram http://doc.akka.io/docs/akka/snapshot/common/cluster.html#State_Diagram_for_the_Member_States__akka_cluster_allow-weakly-up-members=off_
+New leader marks old node as UNREACHABLE after the heartbeat timeout which then removes it from the cluster.

+I Have tried using graceful shutdown with even less success, this seems to cause the actor system to close before handoff complete.
