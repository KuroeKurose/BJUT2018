//package scale
//
//import chisel3._
//import chisel3.util._
//
//class Cache extends Module with Params with CurrentCycle {
//  val io = IO(new CacheIO)
//
//  val sIdle :: sInitBusy :: sInitDone :: Nil = Enum(3)
//
//  val state = RegInit(sIdle)
//
//  val initSetCounter = new Counter(numSets)
//
//  val data = Mem(numSets, Vec(assoc, UInt(blockSize.W)))
//
//  val tags = VecInit(Seq.fill(numSets*assoc)(UInt(tagWidth.W)))
//
//  val readHits = new Counter(Int.MaxValue)
//  val writeHits = new Counter(Int.MaxValue)
//  val readMisses = new Counter(Int.MaxValue)
//  val writeMisses = new Counter(Int.MaxValue)
//  val replacements = new Counter(Int.MaxValue)
//
//  io.cpuResp.valid := false.B
//  io.cpuResp.bits := DontCare
//
//  io.memReq.valid := false.B
//  io.memReq.bits := DontCare
//
//  val lfus = Seq.tabulate(numSets)(setIndex => new LFU(setIndex, assoc))
//
//  def init(setIndex: UInt): Unit = {
//    val set = Wire(Vec(assoc, new CacheBlock))
//
//    (0 until assoc).foreach { i =>
//      set(i).valid := false.B
//      set(i).tag := 0.U
//      set(i).data := 0.U
//    }
//
//    blocks(setIndex) := set
//
//    //    printf(p"[$currentCycle] cache.sets($setIndex).init\n")
//  }
//
//  switch(state) {
//    is(sIdle) {
//      init(initSetCounter.value)
//
//      state := sInitBusy
//    }
//    is(sInitBusy) {
//      init(initSetCounter.value)
//
//      when(initSetCounter.inc()) {
//        state := sInitDone
//        printf(p"[$currentCycle] cache.init done.\n")
//      }
//    }
//  }
//
//  val addr = RegNext(io.cpuReq.bits.addr)
//  val setIndex = RegNext(io.cpuReq.bits.addr.setIndex)
//  val tag = RegNext(io.cpuReq.bits.addr.tag)
//
//  io.readHits := readHits.value
//  io.readMisses := readMisses.value
//  io.writeHits := writeHits.value
//  io.writeMisses := writeMisses.value
//  io.replacements := replacements.value
//
//
//  when(state === sInitDone && io.cpuReq.valid) {
//    io.cpuResp.valid := true.B
//    val hit: Bool = blocks(setIndex).exists((block: CacheBlock) => block.valid && block.tag === tag)
//
//
//    when(hit) {
//      val way: UInt = blocks(setIndex).indexWhere((block: CacheBlock) => block.valid && block.tag === tag)
//
//      lfus.zipWithIndex.foreach { case (lfu, i) =>
//        when(i.U === setIndex) {
//          lfu.hit(way)
//        }
//      }
//      when(io.cpuReq.bits.read) {
//        val blockFound = blocks(setIndex)(way)
//
//        readHits.inc()
//        printf(p"[$currentCycle] cache.sets($setIndex)($way).read hit: addr = 0x${Hexadecimal(addr)}, tag = 0x${Hexadecimal(tag)}, blockFound = 0x${Hexadecimal(blockFound.data)}\n")
//
//        io.cpuResp.bits.data := blockFound.data
//      }.otherwise {
//        val newBlock = Wire(new CacheBlock)
//        newBlock.valid := true.B
//        newBlock.tag := tag
//        newBlock.data := io.cpuReq.bits.data
//
//        writeHits.inc()
//        printf(p"[$currentCycle] cache.sets($setIndex)($way).write hit: addr = 0x${Hexadecimal(addr)}, tag = 0x${Hexadecimal(tag)}, newBlock = 0x${Hexadecimal(newBlock.data)}\n")
//
//        blocks(setIndex)(way) := newBlock
//      }
//    }.otherwise { //when miss
//      when(io.cpuReq.bits.read && io.memResp.valid) {
//        lfus.zipWithIndex.foreach { case (lfu, i) =>
//          when(i.U === setIndex) {
//            val victimWay = lfu.miss()
//
//            io.memReq.valid := true.B
//            io.memReq.bits.read := true.B
//            io.memReq.bits.addr := addr
//
//            val victimBlock = blocks(setIndex)(victimWay)
//            when(victimBlock.valid) {
//              replacements.inc()
//            }
//
//            val newBlock = Wire(new CacheBlock)
//            newBlock.valid := true.B
//            newBlock.tag := tag
//            newBlock.data := io.memResp.bits.data
//
//            readMisses.inc()
//            printf(p"[$currentCycle] cache.sets($setIndex)($victimWay).read miss: addr = 0x${Hexadecimal(addr)}, tag = 0x${Hexadecimal(tag)}, victimBlock = 0x${Hexadecimal(victimBlock.data)}, newBlock = 0x${Hexadecimal(newBlock.data)}\n")
//
//            blocks(setIndex)(victimWay) := newBlock
//
//            io.cpuResp.bits.data := newBlock.data
//          }
//        }
//      }.elsewhen(!io.cpuReq.bits.read) {
//        lfus.zipWithIndex.foreach { case (lfu, i) =>
//          when(i.U === setIndex) {
//            val victimWay = lfu.miss()
//            val victimBlock = blocks(setIndex)(victimWay)
//            when(victimBlock.valid) {
//              replacements.inc()
//            }
//            io.memReq.valid := true.B
//            io.memReq.bits.read := false.B
//            io.memReq.bits.addr := Cat(tag + setIndex + 0.U(offsetWidth.W))
//            io.memReq.bits.data := victimBlock.data
//
//            val newBlock = Wire(new CacheBlock)
//            newBlock.valid := true.B
//            newBlock.tag := tag
//            newBlock.data := io.cpuReq.bits.data
//
//            writeMisses.inc()
//            printf(p"[$currentCycle] cache.sets($setIndex)($victimWay).write miss: addr = 0x${Hexadecimal(addr)}, tag = 0x${Hexadecimal(tag)}, victimBlock = 0x${Hexadecimal(victimBlock.data)}, newBlock = 0x${Hexadecimal(newBlock.data)}\n")
//
//
//            blocks(setIndex)(victimWay) := newBlock
//
//          }
//        }
//      }
//    }
//  }
//}
//
//object Cache extends App {
//  Driver.execute(Array("-td", "source/"), () => new Cache)
//}