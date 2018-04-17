package scale

import chisel3._
import chisel3.util._

class MemIO extends Bundle {
  val req = Flipped(Valid(new MemRequest))
  val resp = Valid(new MemResponse)
}


