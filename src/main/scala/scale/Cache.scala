package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params {
  val io = IO(new CacheIO)

  val ren = io.cpuReq.valid
  val ren_reg = RegNext(ren)
  val blocks = Mem(numSets, VecInit(Seq.fill(assoc)(new CacheBlock)))
  val counter_LFU = Mem(numSets, VecInit(Seq.fill(assoc)(0.U(4.W))))

  io.memReq.valid := false.B
  io.memReq.bits.read := false.B
  io.memReq.bits.addr := DontCare
  io.cpuResp.valid := false.B
  io.cpuResp.bits := DontCare

  val addr_reg = RegInit(io.cpuReq.bits.addr)
  val setIndex = io.cpuReq.bits.addr.setIndex //need change to reg?
  val tag = io.cpuReq.bits.addr.tag

  val sets = blocks(setIndex)

  val hit: Bool = sets.exists((block: CacheBlock) => block.valid && block.tag === tag)
  // read
  when(hit && ren_reg) {
    val blockFoundId: UInt = sets.indexWhere((block: CacheBlock) => block.valid && block.tag === tag)
    val blockFound = sets(blockFoundId)

    for (n <- 0 until assoc - 1) {
      counter_LFU(setIndex)(n) := counter_LFU(setIndex)(n) + 1.U //fixthis
    }
    counter_LFU(setIndex)(blockFoundId) := 0.U

    io.cpuResp.valid := true.B
    io.cpuResp.bits.data := blockFound.data

  }.elsewhen(!hit && ren_reg) {
    val topCountID = 0.U
    val tempCount = 0.U

    for (n <- 0 until assoc - 1) {
      when(counter_LFU(setIndex)(n) > tempCount) {
        topCountID := n.asUInt()
        tempCount := counter_LFU(setIndex)(n)
      }
      counter_LFU(setIndex)(n) := counter_LFU(setIndex)(n) + 1.U
    }
    //todo:send read request to mem
    io.memReq.valid := true.B
    io.memReq.bits.read := true.B
    io.memReq.bits.addr := addr_reg

    when(io.memResp.valid){    blocks(setIndex)(topCountID) := io.memResp.bits.data
    }

    counter_LFU(setIndex)(topCountID) := 0.U
  }


}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}