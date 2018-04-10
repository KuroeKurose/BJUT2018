package scale

import chisel3.iotesters.PeekPokeTester

class CacheTester(cache: Cache) extends PeekPokeTester(cache) {
  poke(cache.io.cpuReq.valid, false)
  poke(cache.io.cpuReq.bits.read, true)
  poke(cache.io.cpuReq.bits.addr, 0)

  poke(cache.io.cpuResp.ready, true)


}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
