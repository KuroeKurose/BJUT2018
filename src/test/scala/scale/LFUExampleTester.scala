package scale

import chisel3._
import chisel3.util._
import chisel3.iotesters.PeekPokeTester

class LFUExampleIO(assoc: Int) extends Bundle {
  val hitReq = Flipped(Valid(UInt(log2Ceil(assoc).W)))
  val missReq = Flipped(Valid(UInt(1.W)))
  val missResp = Valid(UInt(log2Ceil(assoc).W))
}

class LFUExample(setIndex: Int, assoc: Int) extends Module with CurrentCycle {
  val io = IO(new LFUExampleIO(assoc))

  val lfu = new LFU(setIndex, assoc)

  io.missResp.valid := false.B
  io.missResp.bits := 0.U

  when(io.hitReq.valid) {
    lfu.hit(io.hitReq.bits)
  }

  when(io.missReq.valid) {
    io.missResp.valid := true.B
    io.missResp.bits := lfu.miss()

    printf(p"[$currentCycle] cache.sets($setIndex).lfu.miss: victimWay = ${io.missResp.bits}\n")
  }
}

class LFUExampleTester(example: LFUExample) extends PeekPokeTester(example) {
  poke(example.io.missReq.valid, false)

  poke(example.io.hitReq.valid, true)
  poke(example.io.hitReq.bits, 0)

  step(1)

  poke(example.io.hitReq.bits, 1)

  step(1)

  poke(example.io.hitReq.bits, 6)

  step(1)

  poke(example.io.hitReq.bits, 7)

  step(1)

  poke(example.io.hitReq.valid, false)

  poke(example.io.missReq.valid, true)

  //  while (peek(example.io.missResp.valid) == 0) {
  //    step(1)
  //  }

  expect(example.io.missResp.bits, 1)

  step(1)

  poke(example.io.missReq.valid, false)

  step(10)

}

object LFUExampleTester extends App {
  private val setIndex = 0
  private val assoc = 8

  chisel3.iotesters.Driver(() => new LFUExample(setIndex, assoc)) { example => new LFUExampleTester(example) }
}
