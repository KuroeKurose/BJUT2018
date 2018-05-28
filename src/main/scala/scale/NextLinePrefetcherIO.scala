package scale

import chisel3._
import chisel3.util._

class NextLinePrefetcherRequest extends Bundle with Params {
  val read = Bool()
  val hit = Bool()
  val addr = UInt(addrWidth.W)
}

class NextLinePrefetcherResponse extends Bundle with Params {
  val addr = UInt(addrWidth.W)
}


class NextLinePrefetcherIO extends Bundle {
  val request = Flipped(Valid(new NextLinePrefetcherRequest))
  val response = Valid(new NextLinePrefetcherResponse)
}