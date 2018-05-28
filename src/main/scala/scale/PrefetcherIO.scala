package scale

import chisel3._
import chisel3.util._

class PrefetcherRequest extends Bundle with Params {
  val read = Bool()
  val hit = Bool()
  val addr = UInt(addrWidth.W)
}



class PrefetcherIO extends Bundle {
  val request = Flipped(Decoupled(new PrefetcherRequest))
}