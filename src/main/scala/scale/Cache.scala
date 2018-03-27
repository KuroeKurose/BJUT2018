package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params {
  val io = IO(new CacheIO)

  val blocks = Vec(numSets, Vec(assoc, new CacheBlock))

  io.resp.valid := false.B
  io.resp.bits := DontCare

  when(io.req.valid) {
    val setIndex = io.req.bits.addr.setIndex
    val tag = io.req.bits.addr.tag

    val sets = blocks(setIndex)

    val blockIsFound:Bool = sets.exists((block:CacheBlock) => block.valid && block.tag === tag)

    when (blockIsFound) {
      val blockFoundId:UInt = sets.indexWhere((block:CacheBlock) => block.valid && block.tag === tag)
      val blockFound = sets(blockFoundId)

      io.resp.valid := true.B
      io.resp.bits.data := blockFound.data
    }
  }
}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}