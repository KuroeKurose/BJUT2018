package scale

import chisel3._
import chisel3.util._

class CacheBlock extends Bundle with Params {
  val valid = Bool()
  val tag = UInt(tagWidth.W)
  val data = UInt(blockSize.W)
}
