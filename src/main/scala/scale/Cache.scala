package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params with CurrentCycle {
  val io = IO(new CacheIO)

  val sIdle :: sInitBusy :: sInitDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val initSetCounter = new Counter(numSets)

  val blocks = Mem(numSets, Vec(assoc, new CacheBlock))

  io.cpuReq.ready := state === sInitDone

  io.cpuResp.valid := false.B
  io.cpuResp.bits := DontCare

  io.memReq.valid := false.B
  io.memReq.bits.read := false.B
  io.memReq.bits.addr := DontCare

  val lfus = Seq.tabulate(numSets)(setIndex => new LFU(setIndex, assoc))

  def init(setIndex: UInt): Unit = {
    val set = Wire(Vec(assoc, new CacheBlock))

    (0 until assoc).foreach { i =>
      set(i).valid := 0.U
      set(i).tag := 0.U
      set(i).data := 0.U
    }

    blocks(setIndex) := set

    printf(p"[$currentCycle] cache.sets($setIndex).init\n")
  }

  switch(state) {
    is(sIdle) {
      init(initSetCounter.value)

      state := sInitBusy
    }
    is(sInitBusy) {
      init(initSetCounter.value)

      when(initSetCounter.inc()) {
        state := sInitDone
      }
    }
  }

  when(io.cpuReq.valid) {
    val addr = io.cpuReq.bits.addr
    val setIndex = io.cpuReq.bits.addr.setIndex
    val tag = io.cpuReq.bits.addr.tag

    val hit: Bool = blocks(setIndex).exists((block: CacheBlock) => block.valid && block.tag === tag)

    when(hit) {
      val way: UInt = blocks(setIndex).indexWhere((block: CacheBlock) => block.valid && block.tag === tag)
      val blockFound = blocks(setIndex)(way)

      lfus.zipWithIndex.foreach { case (lfu, i) =>
        when(i.U === setIndex) {
          lfu.hit(way)
        }
      }

      io.cpuResp.valid := true.B
      io.cpuResp.bits.data := blockFound.data
    }.otherwise {
      lfus.zipWithIndex.foreach { case (lfu, i) =>
        when(i.U === setIndex) {
          val victimWay = lfu.miss()
          val blockFound = WireInit(blocks(setIndex)(victimWay))

          io.memReq.valid := true.B
          io.memReq.bits.read := true.B
          io.memReq.bits.addr := addr

          blockFound.valid := true.B
          blockFound.tag := tag
          blockFound.data := io.memResp.bits.data

          blocks(setIndex)(victimWay) := blockFound

          io.cpuResp.valid := true.B
          io.cpuResp.bits.data := blockFound.data
        }
      }
    }
  }
}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}