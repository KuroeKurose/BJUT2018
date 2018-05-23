package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

class MemTester(mem:Mem) extends PeekPokeTester(mem){
  poke(mem.io.req.valid, true)
  poke(mem.io.req.bits.read, true)
  poke(mem.io.req.bits.addr, 0)

  while (peek(mem.io.resp.valid) == BigInt(0)) {
    step(1)
  }

  poke(mem.io.req.bits.read, false)
  poke(mem.io.req.bits.data, 1)

  step(1)

  poke(mem.io.req.bits.read, true)

  step(1)

  poke(mem.io.req.valid, false)

  step(1)


}
object MemTester extends App {
  chisel3.iotesters.Driver(() => new Mem) { mem => new MemTester(mem) }
}

