package scale

import chisel3._
import chisel3.util._

class Mem extends Module with Params {
  val io = IO(new MemIO)

  val blocks = Mem(memSize, UInt(blockSize.W))

  io.resp.valid := io.req.valid && io.req.bits.read
  io.resp.bits.data := blocks(io.req.bits.addr)

  when(io.req.valid && !io.req.bits.read) {
    blocks(io.req.bits.addr) := io.req.bits.data
  }
}
