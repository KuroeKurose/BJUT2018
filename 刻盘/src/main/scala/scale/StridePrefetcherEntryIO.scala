package scale

import chisel3._
import chisel3.util._

class StridePrefetcherEntryRequest extends Bundle with Params {
  val pc = UInt(addrWidth.W)
  val effectiveAddress = UInt(addrWidth.W)
}

class StridePrefetcherEntryResponse extends Bundle with Params {
  val prefetchValid = Bool()
  val pc = UInt(addrWidth.W)
  val state = UInt(2.W)
  val prefetchTarget = UInt(addrWidth.W)
}


class StridePrefetcherEntryIO extends Bundle with Params {
  val entryInput = Input(new StridePrefetcherEntryRequest)
  val entryOutput = Output(new StridePrefetcherEntryResponse)
}
