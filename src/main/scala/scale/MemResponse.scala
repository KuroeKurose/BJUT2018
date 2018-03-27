package scale

import chisel3._
import chisel3.util._

class MemResponse extends Bundle with Params {
  val data = UInt(blockSize.W)
}
