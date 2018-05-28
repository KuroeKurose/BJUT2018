package scale

import chisel3._
import chisel3.util._

class NextLinePrefetcher extends Module with Params with CurrentCycle {
  val io = IO(new NextLinePrefetcherIO)
  //
  //  io.memReq.valid := false.B
  //  io.memReq.bits.read := false.B
  //  io.memReq.bits.addr := DontCare
  //
  //  io.cacheReq.valid := false.B
  //  io.cacheReq.bits.read := false.B
  //  io.cacheReq.bits.addr := DontCare
  //  io.cacheReq.bits.data := DontCare
  //
  //  when(io.cpuReq.valid) {
  //    val addr = io.cpuReq.bits.addr
  //    addr := addr + addrWidth.U
  //
  //    io.memReq.valid := true.B
  //    io.memReq.bits.read := true.B
  //    io.memReq.bits.addr := addr
  //
  //    when(io.memResp.valid) {
  //      io.memReq.valid := false.B
  //      val data = io.memResp.bits.data
  //
  //      io.cacheReq.valid := true.B
  //      io.cacheReq.bits.addr := addr
  //      io.cacheReq.bits.data := data
  //      when(io.cacheReq.ready) {
  //        io.cacheReq.valid := false.B
  //      }
  //    }
  //  }


  io.response.valid := false.B
  io.response.bits.addr := 0.U

  when(io.request.valid) {
    io.response.valid := true.B
    io.response.bits.addr := io.request.bits.addr + addrWidth.U
    printf(p"[$currentCycle] response addr = ${io.response.bits.addr}\n")
  }

}

object NextLinePrefetcher extends App {
  Driver.execute(Array("-td", "source/"), () => new NextLinePrefetcher)
}
