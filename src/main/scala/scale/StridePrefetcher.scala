package scale

import chisel3._
import chisel3.util._

class StridePrefetcher extends Module with Params with CurrentCycle {
  val io = IO(new PrefetcherIO)

  val entries = Seq.fill(stridePrefetcherTableSize)(new StridePrefetcherEntry)

  entries.zipWithIndex.foreach { case (entry, i) =>
  }
}


