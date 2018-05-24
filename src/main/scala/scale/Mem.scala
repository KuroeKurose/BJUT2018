package scale

import chisel3._
import chisel3.util._

class Mem extends Module with Params with CurrentCycle {
  val io = IO(new MemIO)

  val sIdle :: sInitBusy :: sInitDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val initCounter = new Counter(memSize)

  val blocks = Mem(memSize, UInt(blockSize.W))

  io.cacheResp.valid := false.B
  io.cacheResp.bits.data := DontCare

  def init(index: UInt): Unit = {
    val memBlock = Wire(UInt(blockSize.W))

    memBlock := 0.U(blockSize.W)

    blocks(index) := memBlock
  }

  switch(state) {
    is(sIdle) {
      init(initCounter.value)

      state := sInitBusy
    }
    is(sInitBusy) {
      init(initCounter.value)

      when(initCounter.inc()) {
        state := sInitDone
        printf(p"[$currentCycle] memory.init done.\n")
      }
    }
  }


  when(state === sInitDone && io.cacheReq.valid && !io.cacheReq.bits.read) {
    io.cacheResp.valid := true.B
    blocks(io.cacheReq.bits.addr) := io.cacheReq.bits.data
    printf(p"[$currentCycle] Mem.write: addr = ${io.cacheReq.bits.addr}, data = ${io.cacheReq.bits.data}\n")

  }.elsewhen(state === sInitDone && io.cacheReq.valid && io.cacheReq.bits.read) {
    io.cacheResp.valid := true.B
    io.cacheResp.bits.data := blocks(io.cacheReq.bits.addr)
    printf(p"[$currentCycle] Mem.read: addr = ${io.cacheReq.bits.addr}, data = ${io.cacheResp.bits.data}\n")

  }
}
