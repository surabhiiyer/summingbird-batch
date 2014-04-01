package com.twitter.summingbird.example

import com.twitter.bijection.{ Codec, Injection } 
import com.twitter.summingbird.batch.BatchID

import java.util.Arrays

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.{ BytesWritable, SequenceFile }
import org.apache.hadoop.fs.{ FileSystem, Path }

object SequenceFileReader {
  import Serialization._

  def deserialize[V](bytes: Array[Byte])(implicit inj: Injection[(BatchID, V), Array[Byte]], c: Codec[V]) = inj.invert(bytes)

  def main(args: Array[String]) {
    val path = new Path("/Users/surabhi.ravishankar/summingbird-batch/summingbird-example/src/main/scala/com/twitter/summingbird/example/store/1396350000000/part-00000")
    val config = new Configuration()
    val reader = new SequenceFile.Reader(FileSystem.get(config), path, config)
    val key = new BytesWritable()
    val value = new BytesWritable() 
    while (reader.next(key, value)) {
      val valueBytes = Arrays.copyOfRange(value.getBytes, 0, value.getLength)
      println(new String(key.getBytes, 0, key.getLength) + " : " + deserialize[Long](valueBytes).get._2)
    }
    reader.close()
  }
} 
