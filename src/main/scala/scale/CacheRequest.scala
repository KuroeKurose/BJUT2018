package scale

import chisel3._
import chisel3.util._

class CacheRequest extends Bundle with Params {
  val read = Bool()
  val addr = UInt(addrWidth.W)
  val data = UInt(blockSize.W)
}
