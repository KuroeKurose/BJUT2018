package scale

import chisel3._
import chisel3.util._

class Cache extends Module with Params {
  val io = IO(new CacheIO)

  val ren = io.cpuReq.valid
  val ren_reg = RegInit(ren)

  val blocks = Mem(numSets, Vec(assoc, new CacheBlock))

  io.memResp.ready := false.B
  io.memReq.valid := false.B
  io.memReq.bits.read := false.B
  io.memReq.bits.addr := DontCare
  io.cpuResp.valid := false.B
  io.cpuResp.bits := DontCare
  io.cpuReq.ready := true.B


  //  val counter_LFU = Mem(numSets, Vec(assoc, UInt(4.W))) //todo init

  val counterLFU = Seq.fill(numSets)(Seq.fill(assoc)(new Counter(32)))

  when(io.cpuReq.valid) {
    val addr_reg = RegInit(io.cpuReq.bits.addr)
    val setIndex_reg = RegInit(io.cpuReq.bits.addr.setIndex) //need change to reg?
    val tag_reg = RegInit(io.cpuReq.bits.addr.tag)

    val set = blocks.read(setIndex_reg)

    val hit: Bool = set.exists((block: CacheBlock) => block.valid && block.tag === tag_reg)

    when(hit) {
      val blockFoundWay: UInt = set.indexWhere((block: CacheBlock) => block.valid && block.tag === tag_reg)
      val blockFound = set(blockFoundWay)

      counterLFU.zipWithIndex.foreach { case (_, i) =>
        when(i.U === setIndex_reg) {
          counterLFU(i).zipWithIndex.foreach { case (_, j) =>
            when(j.U === blockFoundWay) {
              counterLFU(i)(j).value := 0.U
            }.otherwise {
              counterLFU(i)(j).inc()
            }

          }
        }
      }

      io.cpuResp.valid := true.B
      io.cpuResp.bits.data := blockFound.data
    }.otherwise {
//      val victimWay = RegInit(0.U)
      counterLFU.zipWithIndex.foreach { case (_, i) =>
        when(i.asUInt() === setIndex_reg) {
          val tempCount = RegInit(0.U(4.W))
          counterLFU(i).zipWithIndex.foreach { case (_, j) =>
            when(counterLFU(i)(j).value > tempCount) {
//              victimWay := j.asUInt()
              tempCount := counterLFU(i)(j).value
            }.otherwise {
              counterLFU(i)(j).inc()
            }
          }
          counterLFU(i).zipWithIndex.foreach { case (_, k) =>
            when(counterLFU(i)(k).value === tempCount) {
              counterLFU(i)(k).value := 0.U
            }
          }
        }
      }
      when(io.memReq.ready) {
        io.memReq.valid := true.B
        io.memReq.bits.read := true.B
        io.memReq.bits.addr := addr_reg
        io.memResp.ready := true.B
      }
      when(io.memResp.valid) {
        val oldval = blocks.read(setIndex_reg)
        val newval = oldval
        //todo victimWay
//        newval(victimWay).valid := true.B
//        newval(victimWay).dirty := false.B
//        newval(victimWay).tag := tag_reg
//        newval(victimWay).data := io.memResp.bits.data
//        blocks.write(setIndex_reg, newval)
//
//        io.cpuResp.bits.data:= io.memResp.bits.data
      }
    }

  }

  //  // read
  //  when(hit && ren_reg) {
  //
  //    for (n <- 0 until assoc - 1) {
  //      counter_LFU(setIndex)(n) := counter_LFU(setIndex)(n) + 1.U //fixthis
  //    }
  //    counter_LFU(setIndex)(blockFoundId) := 0.U
  //
  //    io.cpuResp.valid := true.B
  //    io.cpuResp.bits.data := blockFound.data
  //
  //  }.elsewhen(!hit && ren_reg) {
  //    val topCountID = Wire(UInt())
  //    val tempCount = Wire(UInt())
  //
  //    topCountID := 0.U
  //    tempCount := 0.U
  //
  //    for (n <- 0 until assoc - 1) {
  //      when(counter_LFU(setIndex)(n) > tempCount) {
  //        topCountID := n.asUInt()
  //        tempCount := counter_LFU(setIndex)(n)
  //      }
  //      counter_LFU(setIndex)(n) := counter_LFU(setIndex)(n) + 1.U
  //    }
  //
  //    val memData = new CacheBlock
  //    io.memReq.valid := true.B
  //    io.memReq.bits.read := true.B
  //    io.memReq.bits.addr := addr_reg
  //
  //    when(true.B) {
  //      val oldval = blocks.read(setIndex)
  //      val newval = oldval
  //      newval(topCountID) := memData
  //      blocks.write(setIndex, newval)
  //    }
  //
  //    counter_LFU(setIndex)(topCountID) := 0.U
  //  }


}

object Cache extends App {
  Driver.execute(Array("-td", "source/"), () => new Cache)
}