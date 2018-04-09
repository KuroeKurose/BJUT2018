package scale

import chisel3._
import chisel3.util._

class MemIO extends Bundle {
  val cpuReq = Flipped(Valid(new MemRequest))

  val cacheReq = Flipped(Valid(new MemRequest))
  val cacheResp = Valid(new MemResponse)
}


