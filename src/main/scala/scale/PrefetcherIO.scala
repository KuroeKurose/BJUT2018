package scale

import chisel3._
import chisel3.util._

class PrefetcherRequest extends Bundle with Params {
  val pc = UInt(addrWidth.W)
  val effectiveAddress = UInt(addrWidth.W)
}

class PrefetcherResponse extends Bundle with Params {
  val prefetchTarget = UInt(addrWidth.W)
}

class PrefetcherIO extends Bundle {
  val request = Flipped(Valid(new PrefetcherRequest))
  val response = Valid(new PrefetcherResponse)
}