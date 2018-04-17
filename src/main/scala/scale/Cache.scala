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

  val lfus = Seq.tabulate(numSets)(setIndex => new LFU(setIndex, assoc))

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