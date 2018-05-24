package scale

import chisel3._
import chisel3.util._

class PrefetcherIO extends Bundle {
  val cpuReqMonitor = Flipped(Decoupled(new CacheRequest))

  val cacheReq = Decoupled(new CacheRequest)

  val memReq = Valid(new CacheRequest)
  val memResp = Flipped(Valid(new CacheResponse))

}
