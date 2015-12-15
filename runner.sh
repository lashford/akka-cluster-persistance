#!/bin/bash

sbt "runMain com.gvolpe.cluster.Dc1Cluster" &
sbt "runMain com.gvolpe.cluster.Dc2Cluster" &
