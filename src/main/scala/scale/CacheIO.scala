package scale

import chisel3._
import chisel3.util._

class CacheIO extends Bundle {
  val cpuReq = Flipped(Decoupled(new CacheRequest))
  val cpuResp = Decoupled(new CacheResponse)

  val memReq = Valid(new CacheRequest)
  val memResp = Flipped(Valid(new CacheResponse))
}
