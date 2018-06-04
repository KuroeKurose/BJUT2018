package scale

import chisel3._
import chisel3.util._

class NextLinePrefetcher extends Module with Params with CurrentCycle {
  val io = IO(new PrefetcherIO)

  io.response.valid := false.B
  io.response.bits.prefetchTarget := 0.U

  when(io.request.valid) {
    io.response.valid := true.B
    io.response.bits.prefetchTarget := io.request.bits.effectiveAddress + addrWidth.U

    printf(p"[$currentCycle] response addr = ${io.response.bits.prefetchTarget}\n")
  }
}

object NextLinePrefetcher extends App {
  Driver.execute(Array("-td", "source/"), () => new NextLinePrefetcher)
}
