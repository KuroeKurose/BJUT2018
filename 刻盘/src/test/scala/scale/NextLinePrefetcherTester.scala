package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester


class NextLinePrefetcherTester(prefetcher: NextLinePrefetcher) extends PeekPokeTester(prefetcher) with Params {
  var addr = BigInt(0)
  poke(prefetcher.io.request.valid, false)
  poke(prefetcher.io.request.bits.effectiveAddress, 0.U)
  step(1)

  for (i <- 0 until 5) {
    poke(prefetcher.io.request.valid, true)
    poke(prefetcher.io.request.bits.effectiveAddress, i.U)

    while (peek(prefetcher.io.response.valid) == 0) {
      step(1)
    }
    addr = peek(prefetcher.io.response.bits.prefetchTarget)
    step(1)
    poke(prefetcher.io.request.valid, false)
  }


}

object NextLinePrefetcherTester extends App {
  chisel3.iotesters.Driver(() => new NextLinePrefetcher) { prefetcher => new NextLinePrefetcherTester(prefetcher) }
}