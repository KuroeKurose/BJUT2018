package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params {
  val io = IO(new CacheIO)

  val ren = io.req.valid
  val ren_reg = RegNext(ren)
  val blocks = Vec(numSets, Vec(assoc, new CacheBlock))

  val counter_LFU = Vec(numSets, VecInit(Seq.fill(assoc)(0.U(4.W))))

  io.resp.valid := false.B
  io.resp.bits := DontCare

  val setIndex = io.req.bits.addr.setIndex
  val tag = io.req.bits.addr.tag

  val sets = blocks(setIndex)

  val hit: Bool = sets.exists((block: CacheBlock) => block.valid && block.tag === tag)

  // read
  when(hit && ren_reg) {
    val blockFoundId: UInt = sets.indexWhere((block: CacheBlock) => block.valid && block.tag === tag)
    val blockFound = sets(blockFoundId)

    for (n <- 0 until assoc - 1) {
      counter_LFU(setIndex)(n) = counter_LFU(setIndex)(n) + 1.U
    }
    counter_LFU(setIndex)(blockFoundId) = 0.U

    io.resp.valid := true.B
    io.resp.bits.data := blockFound.data

  }.elsewhen(!hit && ren_reg) {
    val topCountID = 0.U
    val tempCount = 0.U

    for (n <- 0 until assoc - 1) {
      when(counter_LFU(setIndex)(n) > tempCount) {
        topCountID := n.asUInt()
        tempCount := counter_LFU(setIndex)(n)
      }
      counter_LFU(setIndex)(n) = counter_LFU(setIndex)(n) + 1.U
    }
    //todo:send read request to mem

    val memData = Reg(io.resp.bits.data.cloneType)
    blocks(setIndex)(topCountID) = memData

    counter_LFU(setIndex)(topCountID) = 0.U
  }


}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}