package scale

import chisel3.iotesters.PeekPokeTester

class CacheTester(cache: Cache) extends PeekPokeTester(cache) {
  poke(cache.io.cpuReq.valid, false)


}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
