package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

// todo accesses, read/write hits, misses, hit rate, replacements
class CacheTester(cache: Cache) extends PeekPokeTester(cache) {
  for( i <- 0 until 100){
    poke(cache.io.cpuReq.valid, true)
    poke(cache.io.cpuReq.bits.read, rnd.nextInt(1))
    poke(cache.io.cpuReq.bits.addr, rnd.nextInt(Int.MaxValue))
    poke(cache.io.cpuReq.bits.data, rnd.nextInt(Int.MaxValue))

    poke(cache.io.cpuResp.ready, true)
    poke(cache.io.memResp.valid, true)
    poke(cache.io.memResp.bits.data, rnd.nextInt(Int.MaxValue))

    while (peek(cache.io.cpuReq.ready) == BigInt(0)) {
      step(1)
    }

    while (peek(cache.io.cpuResp.valid) == BigInt(0)) {
      step(1)
    }

    poke(cache.io.cpuReq.valid, false)

    step(1)
  }
}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
