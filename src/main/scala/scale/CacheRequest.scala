package scale

import chisel3._
import chisel3.util._

class CacheRequest extends Bundle with MemParams {
  val addr = UInt(addrWidth.W)
}
