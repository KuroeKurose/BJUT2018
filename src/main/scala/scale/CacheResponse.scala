package scale

import chisel3._
import chisel3.util._

class CacheResponse extends Bundle with CacheParams {
  val data = UInt(blockSize.W)
}
