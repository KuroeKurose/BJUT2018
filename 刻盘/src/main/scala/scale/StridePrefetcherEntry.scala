package scale

import chisel3._
import chisel3.util._

class StridePrefetcherEntry extends Module with Params with CurrentCycle {
  val io = IO(new StridePrefetcherEntryIO)

  val instructionTag = RegNext(io.entryInput.pc, 0.U)
  io.entryOutput.pc := instructionTag

  val previousAddress = RegNext(io.entryInput.effectiveAddress, 0.U)

  val stride = RegNext(io.entryInput.effectiveAddress - previousAddress, 0.U)

  val correct = stride === RegNext(stride)

  val sInitial :: sSteady :: sTransient :: sNoPrediction :: Nil = Enum(4)
  val state = RegInit(sInitial)
  io.entryOutput.state := sInitial

  io.entryOutput.prefetchValid := false.B
  io.entryOutput.prefetchTarget := 0.U


  io.entryOutput.prefetchValid := state === sSteady || state === sTransient
  io.entryOutput.prefetchTarget := io.entryInput.effectiveAddress + stride

  switch(state) {
    is(sInitial) {
      when(correct) {
        state := sSteady
        io.entryOutput.prefetchValid := true.B
      }.otherwise {
        state := sTransient
        io.entryOutput.prefetchValid := true.B
      }
    }
    is(sSteady) {
      when(correct) {
        //no action
        io.entryOutput.prefetchValid := true.B
      }.otherwise {
        state := sInitial
        io.entryOutput.prefetchValid := false.B
      }
    }
    is(sTransient) {
      when(correct) {
        state := sSteady
        io.entryOutput.prefetchValid := true.B
      }.otherwise {
        state := sNoPrediction
        io.entryOutput.prefetchValid := false.B
      }
    }
    is(sNoPrediction) {
      when(correct) {
        state := sTransient
        io.entryOutput.prefetchValid := true.B
      }.otherwise {
        //no action
        io.entryOutput.prefetchValid := false.B
      }
    }
  }
}

object StridePrefetcherEntry extends App {
  Driver.execute(Array("-td", "source/"), () => new StridePrefetcherEntry)
}
