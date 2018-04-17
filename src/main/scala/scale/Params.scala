package scale

import chisel3._
import chisel3.core.UInt
import chisel3.util._

trait Params {
  val addrWidth = 32

  val memSize = 8.MB

  val cacheSize = 64.kB
  val blockSize = 64 * bitsInByte
  val assoc = 8
  val numSets = cacheSize / blockSize / assoc

  val offsetWidth = log2Ceil(blockSize)
  val setIndexWidth = log2Ceil(numSets)
  val tagWidth = addrWidth - offsetWidth - setIndexWidth

  def bitsInByte = 8

  implicit class fromUIntToField(val addr: UInt) {
    def offset = addr(offsetWidth - 1, 0)

    def setIndex = addr(addrWidth - tagWidth - 1, offsetWidth)

    def tag = addr(addrWidth - 1, addrWidth - tagWidth)
  }

  implicit class fromUIntToUnit(val size: Int) {
    def kB = size << 10

    def MB = size << 20

    def GB = size << 30
  }

}