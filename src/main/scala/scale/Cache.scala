package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params with CurrentCycle {
  val io = IO(new CacheIO)

  val sIdle :: sInitBusy :: sInitDone :: Nil = Enum(3)

  val state = RegInit(sIdle)

  val initSetCounter = new Counter(numSets)

  val blocks = Mem(numSets, Vec(assoc, new CacheBlock))

  io.cpuReq.ready := false.B

  io.cpuResp.valid := false.B
  io.cpuResp.bits := DontCare

  io.memReq.valid := false.B
  io.memReq.bits := DontCare

  val lfus = Seq.tabulate(numSets)(setIndex => new LFU(setIndex, assoc))

  def init(setIndex: UInt): Unit = {
    val set = Wire(Vec(assoc, new CacheBlock))

    (0 until assoc).foreach { i =>
      set(i).valid := false.B
      set(i).tag := 0.U
      set(i).data := 0.U
    }

    blocks(setIndex) := set

//    printf(p"[$currentCycle] cache.sets($setIndex).init\n")
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
        printf(p"[$currentCycle] cache.init done.\n")
      }
    }
  }

  val addr = io.cpuReq.bits.addr
  val setIndex = io.cpuReq.bits.addr.setIndex
  val tag = io.cpuReq.bits.addr.tag

  when(state === sInitDone && io.cpuReq.valid && io.cpuResp.ready) {
    io.cpuReq.ready := true.B

    val hit: Bool = blocks(setIndex).exists((block: CacheBlock) => block.valid && block.tag === tag)

    when(hit) {
      val way: UInt = blocks(setIndex).indexWhere((block: CacheBlock) => block.valid && block.tag === tag)

      lfus.zipWithIndex.foreach { case (lfu, i) =>
        when(i.U === setIndex) {
          lfu.hit(way)
        }
      }

      when(io.cpuReq.bits.read) {
        val blockFound = blocks(setIndex)(way)

        printf(p"[$currentCycle] cache.sets($setIndex)($way).read hit: addr = $addr, tag = $tag, blockFound = $blockFound\n")

        io.cpuResp.valid := true.B
        io.cpuResp.bits.data := blockFound.data
      }.otherwise {
        val newBlock = Wire(new CacheBlock)
        newBlock.valid := true.B
        newBlock.tag := tag
        newBlock.data := io.cpuReq.bits.data

        printf(p"[$currentCycle] cache.sets($setIndex)($way).write: addr = $addr, tag = $tag, newBlock = $newBlock\n")

        blocks(setIndex)(way) := newBlock
        io.cpuResp.valid := true.B
      }

    }.otherwise {    //when miss
      when(io.cpuReq.bits.read) {
        lfus.zipWithIndex.foreach { case (lfu, i) =>
          when(i.U === setIndex) {
            val victimWay = lfu.miss()

            io.memReq.valid := true.B
            io.memReq.bits.read := true.B
            io.memReq.bits.addr := addr

            val victimBlock = Wire(new CacheBlock)
            victimBlock.valid := true.B
            victimBlock.tag := tag
            victimBlock.data := io.memResp.bits.data

            printf(p"[$currentCycle] cache.sets($setIndex)($victimWay).read miss: addr = $addr, tag = $tag, victimBlock = $victimBlock\n")

            blocks(setIndex)(victimWay) := victimBlock

            io.cpuResp.valid := true.B
            io.cpuResp.bits.data := victimBlock.data
          }
        }
      }.otherwise {
        lfus.zipWithIndex.foreach { case (lfu, i) =>
          when(i.U === setIndex) {
            val victimWay = lfu.miss()
            val victimBlock = blocks(setIndex)(victimWay)

            io.memReq.valid := true.B
            io.memReq.bits.read := false.B
            io.memReq.bits.addr := Cat(tag + setIndex + 0.U(offsetWidth.W))
            io.memReq.bits.data := victimBlock.data

            val newBlock = Wire(new CacheBlock)
            newBlock.valid := true.B
            newBlock.tag := tag
            newBlock.data := io.cpuReq.bits.data

            printf(p"[$currentCycle] cache.sets($setIndex)($victimWay).write: addr = $addr, tag = $tag, victimBlock = $victimBlock, newBlock = $newBlock\n")


            blocks(setIndex)(victimWay) := newBlock

            io.cpuResp.valid := true.B
          }
        }
      }

    }
  }
}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}