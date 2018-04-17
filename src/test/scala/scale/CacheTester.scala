package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

class CacheTester(cache: Cache) extends PeekPokeTester(cache) {
  poke(cache.io.cpuReq.valid, true)
  poke(cache.io.cpuReq.bits.read, true)
  poke(cache.io.cpuReq.bits.addr, 0)

  poke(cache.io.cpuResp.ready, true)

  poke(cache.io.memResp.valid, true)
  poke(cache.io.memResp.bits.data, 2)

  while (peek(cache.io.cpuReq.ready) == BigInt(0)) {
    step(1)
  }

  while (peek(cache.io.cpuResp.valid) == BigInt(0)) {
    step(1)
  }

  poke(cache.io.cpuReq.valid, false)

  step(1)
}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
