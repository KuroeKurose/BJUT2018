package scale

import chisel3._
import chisel3.util._
import chisel3.iotesters.PeekPokeTester

class LFUExampleIO(assoc: Int) extends Bundle {
  val hitReq = Flipped(Valid(UInt(log2Ceil(assoc).W)))
  val missReq = Flipped(Valid(UInt(1.W)))
  val missResp = Valid(UInt(log2Ceil(assoc).W))
}

class LFUExample(assoc: Int) extends Module {
  val io = IO(new LFUExampleIO(assoc))

  val lfu = new LFU(assoc)

  io.missResp.valid := false.B
  io.missResp.bits := DontCare

  when(io.hitReq.valid) {
    lfu.hit(io.hitReq.bits)
  }

  when(io.missReq.valid) {
    io.missResp.bits := lfu.miss()
  }
}

class LFUExampleTester(example: LFUExample) extends PeekPokeTester(example) {
  poke(example.io.missReq.valid, false)

  poke(example.io.hitReq.valid, true)
  poke(example.io.hitReq.bits, 0)

  step(1)

  poke(example.io.hitReq.valid, false)

  poke(example.io.missReq.valid, true)

  step(1)
}

object LFUExampleTester extends App {
  private val assoc = 8

  chisel3.iotesters.Driver(() => new LFUExample(assoc)) { example => new LFUExampleTester(example) }
}
