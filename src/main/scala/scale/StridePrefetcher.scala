package scale

import chisel3._
import chisel3.util._

class StridePrefetcher extends Module with Params with CurrentCycle {
  val io = IO(new PrefetcherIO)

  io.response.valid := false.B
  io.response.bits.prefetchTarget := 0.U

  val entries = Seq.fill(stridePrefetcherTableSize)(new StridePrefetcherEntry)

  entries.zipWithIndex.foreach { case (entry, i) =>
    entry <> (io.request, io.response)
  }

  val found = WireInit(false.B)

  //  entries.zipWithIndex.foreach { case (entry, i) =>
  //      when(io.request.bits.pc === entry.request.bits.pc){
  //        entry <>(io.request,io.response)
  //        found := true.B
  //      }
  //  }
  //
  //  when(!found){
  //    val replaced = WireInit(false.B)
  //
  //    entries.zipWithIndex.foreach { case (entry, i) =>
  //      when(entry.state === sNoPrediction){
  //        entry <>(io.request,io.response)
  //        found := false.B
  //        replaced := true.B
  //      }
  //    }
  //
  //    when(!replaced){
  //      entries(i) <>(io.request,io.response)
  //    }
  //  }
}

object StridePrefetcher extends App {
  Driver.execute(Array("-td", "source/"), () => new StridePrefetcher)
}


