package scale

import chisel3._
import chisel3.core.UInt
import chisel3.util._

trait CacheParams extends MemParams {
  val capacity = 1 << 20 //1MB
  val blockSize = 1 << 9 //64Byte
  val assoc = 8
  val numSets = capacity / blockSize / assoc

  val offsetWidth = log2Ceil(blockSize)
  val setIndexWidth = log2Ceil(numSets)
  val tagWidth = addrWidth - offsetWidth - setIndexWidth

  implicit class fromAddrToField(val addr:UInt){
    def offset = addr(offsetWidth - 1, 0)
    def setIndex = addr(addrWidth - tagWidth - 1, offsetWidth)
    def tag = addr(addrWidth, addrWidth - tagWidth)
  }
}
