package scale

import chisel3._
import chisel3.util._

class Prefetcher extends Module with Params with CurrentCycle {
  val io = IO(new PrefetcherIO)

  io.memReq.valid := false.B
  io.memReq.bits.read := false.B
  io.memReq.bits.addr := DontCare

  io.cacheReq.valid := false.B
  io.cacheReq.bits.read := false.B
  io.cacheReq.bits.addr := DontCare
  io.cacheReq.bits.data := DontCare

  when(io.cpuReqMonitor.valid) {
    val addr = io.cpuReqMonitor.bits.addr
    addr := addr + addrWidth.U

    io.memReq.valid := true.B
    io.memReq.bits.read := true.B
    io.memReq.bits.addr := addr

    when(io.memResp.valid) {
      io.memReq.valid := false.B
      val data = io.memResp.bits.data

      io.cacheReq.valid := true.B
      io.cacheReq.bits.addr := addr
      io.cacheReq.bits.data := data
      when(io.cacheReq.ready) {
        io.cacheReq.valid := false.B
      }
    }
  }

}
