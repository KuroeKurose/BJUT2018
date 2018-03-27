package scale

import chisel3._
import chisel3.util._

class Mem extends Module with Params {
  val io = IO(new MemIO)

  val blocks = Vec(memSize, UInt(blockSize.W))
}
