package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

class CacheTester(cache: Cache) extends PeekPokeTester(cache) {
  poke(cache.io.cpuReq.valid, false)
  poke(cache.io.cpuReq.bits.read, true)
  poke(cache.io.cpuReq.bits.addr, 12345)
  poke(cache.io.cpuResp.ready, true)

  poke(cache.io.memResp.valid, true)
  poke(cache.io.memResp.bits.data, 1111)
}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
