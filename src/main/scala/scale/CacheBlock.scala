package scale

import chisel3._
import chisel3.util._

class CacheBlock extends Bundle with CacheParams {
  val valid = Bool()
  val dirty = Bool()
  val tag = UInt(tagWidth.W)
  val data = UInt(blockSize.W)
}
