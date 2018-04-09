package scale

import chisel3._
import chisel3.util._

class Mem extends Module with Params {
  val io = IO(new MemIO)

  val addr_reg = RegInit(io.cacheReq.bits.addr)
  val resp_reg = Reg(UInt(blockSize.W))
  val blocks = Mem(memSize, UInt(blockSize.W))

  io.cacheResp.valid := false.B

  when(io.cacheReq.valid && io.cacheReq.bits.read) {
    resp_reg := blocks(addr_reg)
    io.cacheResp.valid := true.B
  }

  io.cacheResp.bits.data := resp_reg
}
