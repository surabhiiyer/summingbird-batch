package com.twitter.summingbird.example

import com.twitter.algebird.monad.ReaderFn
import com.twitter.scalding.{ Hdfs, TextLine }
import com.twitter.summingbird.batch.Timestamp
import com.twitter.summingbird.scalding.{ InitialBatchedStore, Scalding }
import com.twitter.summingbird.scalding.state.HDFSState
import com.twitter.summingbird.scalding.store.VersionedStore
import com.twitter.summingbird.Producer

import org.apache.hadoop.conf.Configuration

object ScaldingRunner {
  final val MillisInHour = 60 * 60 * 1000

  //final val JobDir = "hdfs://172.16.4.147:8020:user/st891x/summingbird"
  
  final val JobDir = "/Users/surabhi.ravishankar/summingbird-batch/summingbird-example/src/main/scala/com/twitter/summingbird/example"
  import Serialization._, StatusStreamer._

  val now = System.currentTimeMillis
  val waitingState = HDFSState(JobDir + "/waitstate", startTime = Some(Timestamp(now - 2 * MillisInHour)), numBatches = 3)
  
  val src: Producer[Scalding, String] = Producer.source[Scalding, String](Scalding.pipeFactoryExact(_ => TextLine(JobDir + "/input.txt")))
  val versionedStore = VersionedStore[String, Long]("/Users/surabhi.ravishankar/summingbird-batch/summingbird-example/src/main/scala/com/twitter/summingbird/example/store")
  val store = new InitialBatchedStore(batcher.currentBatch - 2L, versionedStore)

//	val vbs = new VersionedBatchStore[String, Long, String, Long](inout, 3, batcher)(null)(identity)
//	val store: Scalding#Store[String,Long] = new InitialBatchedStore(batcher.currentBatch - 2L, vbs)

  val mode = Hdfs(false, new Configuration())
    
  def main(args: Array[String]) {
    val job =  Scalding("wordcountJob")
    job.run(waitingState, mode, job.plan(wordCount[Scalding](src, store)))

    // Lookup results
    lookup()
  } 

  def lookup() {
    println("\nRESULTS: \n")

    val results = store.readLast(StatusStreamer.batcher.currentBatch, mode)
    println(results)
  } 
}
