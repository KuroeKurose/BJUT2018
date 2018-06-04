package scale

import chisel3._
import chisel3.util._

class CacheResponse extends Bundle with Params {
  val data = UInt(blockSize.W)
}
