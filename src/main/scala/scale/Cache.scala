package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params {
  val io = IO(new CacheIO)

  val blocks = Mem(numSets, Vec(assoc, new CacheBlock))

  io.cpuReq.ready := true.B

  io.cpuResp.valid := false.B
  io.cpuResp.bits := DontCare

  io.memReq.valid := false.B
  io.memReq.bits.read := false.B
  io.memReq.bits.addr := DontCare

  io.memResp.ready := false.B

  val lfus = Seq.fill(numSets)(new LFU(assoc))

  when(io.cpuReq.valid) {
    val addr = RegInit(io.cpuReq.bits.addr)
    val setIndex = RegInit(io.cpuReq.bits.addr.setIndex)
    val tag = RegInit(io.cpuReq.bits.addr.tag)

    val set = blocks.read(setIndex)

    val hit: Bool = set.exists((block: CacheBlock) => block.valid && block.tag === tag)

    when(hit) {
      val blockFoundWay: UInt = set.indexWhere((block: CacheBlock) => block.valid && block.tag === tag)
      val blockFound = set(blockFoundWay)

      lfus.zipWithIndex.foreach { case (lfu, i) =>
        when(i.U === setIndex) {
          lfu.hit(blockFoundWay)
        }
      }

      io.cpuResp.valid := true.B
      io.cpuResp.bits.data := blockFound.data
    }.otherwise {
      lfus.zipWithIndex.foreach { case (lfu, i) =>
        when(i.U === setIndex) {
          val victimWay = lfu.miss()
        }
      }

      when(io.memReq.ready) {
        io.memReq.valid := true.B
        io.memReq.bits.read := true.B
        io.memReq.bits.addr := addr
        io.memResp.ready := true.B
      }

      when(io.memResp.valid) {
        val oldval = blocks.read(setIndex)
        val newval = oldval
      }
    }
  }
}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}