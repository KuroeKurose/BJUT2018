package scale

import chisel3._
import chisel3.util._

class StridePrefetcherEntry extends Params with CurrentCycle {
  def <>(request: ValidIO[PrefetcherRequest], response: ValidIO[PrefetcherResponse]): Unit = {
    val instructionTag = RegNext(request.bits.pc, 0.U)

    val previousAddress = RegNext(request.bits.effectiveAddress, 0.U)

    val stride = RegNext(request.bits.effectiveAddress - previousAddress, 0.U)

    val correct = stride === RegNext(stride)

    val sInitial :: sSteady :: sTransient :: sNoPrediction :: Nil = Enum(4)
    val state = RegInit(sInitial)

    response.valid := state === sSteady || state === sTransient
    response.bits.prefetchTarget := request.bits.effectiveAddress + stride

    switch(state) {
      is(sInitial) {
        when(correct) {
          state := sSteady
        }.otherwise {
          state := sTransient
        }
      }
      is(sSteady) {
        when(correct) {
          //no action
        }.otherwise {
          state := sInitial
        }
      }
      is(sTransient) {
        when(correct) {
          state := sSteady
        }.otherwise {
          state := sNoPrediction
        }
      }
      is(sNoPrediction) {
        when(correct) {
          state := sTransient
        }.otherwise {
          //no action
        }
      }
    }
  }
}
