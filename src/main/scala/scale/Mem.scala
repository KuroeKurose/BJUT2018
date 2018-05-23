package scale

import chisel3._
import chisel3.util._

class Mem extends Module with Params with CurrentCycle {
  val io = IO(new MemIO)

  val sIdle :: sInitBusy :: sInitDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val initCounter = new Counter(memSize)

  val blocks = Mem(memSize, UInt(blockSize.W))

  io.resp.valid := false.B
  io.resp.bits.data := DontCare

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


  when(state === sInitDone && io.req.valid && !io.req.bits.read) {
    io.resp.valid := true.B
    blocks(io.req.bits.addr) := io.req.bits.data
    printf(p"[$currentCycle] Mem.write: addr = ${io.req.bits.addr}, data = ${io.req.bits.data}\n")

  }.elsewhen(state === sInitDone && io.req.valid && io.req.bits.read) {
    io.resp.valid := true.B
    io.resp.bits.data := blocks(io.req.bits.addr)
    printf(p"[$currentCycle] Mem.read: addr = ${io.req.bits.addr}, data = ${io.resp.bits.data}\n")

  }

  //  when(io.req.valid){
  //    when(io.req.bits.read){
  //      printf(p"[$currentCycle] Mem.read: addr = ${io.req.bits.addr}, data = ${io.resp.bits.data}\n")
  //    }.otherwise{
  //      printf(p"[$currentCycle] Mem.write: addr = ${io.req.bits.addr}, data = ${io.req.bits.data}\n")
  //    }
  //  }
}
