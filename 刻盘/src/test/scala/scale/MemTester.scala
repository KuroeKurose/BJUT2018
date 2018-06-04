package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

class MemTester(mem:Mem) extends PeekPokeTester(mem){
  poke(mem.io.cacheReq.valid, true)
  poke(mem.io.cacheReq.bits.read, true)
  poke(mem.io.cacheReq.bits.addr, 0)

  while (peek(mem.io.cacheResp.valid) == BigInt(0)) {
    step(1)
  }

  poke(mem.io.cacheReq.bits.read, false)
  poke(mem.io.cacheReq.bits.data, 1)

  step(1)

  poke(mem.io.cacheReq.bits.read, true)

  step(1)

  poke(mem.io.cacheReq.valid, false)

  step(1)


}
object MemTester extends App {
  chisel3.iotesters.Driver(() => new Mem) { mem => new MemTester(mem) }
}

