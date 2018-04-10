package scale

import chisel3._
import chisel3.util._

class CacheIO extends Bundle {
  val cpuReq = Flipped(Decoupled(new CacheRequest))
  val cpuResp = Decoupled(new CacheResponse)

  val memReq = Decoupled(new CacheRequest)
  val memResp = Flipped(Decoupled(new CacheResponse))
}
