package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

// todo accesses, read/write hits, misses, hit rate, replacements
class CacheTester(cache: Cache) extends PeekPokeTester(cache) with Params {
  var accesses = 1500
  val testAddr = new Array[Int](100)
  for (j <- 0 until 100) {
    testAddr(j) = rnd.nextInt(Int.MaxValue)
  }

  for (i <- 0 until accesses) {
    poke(cache.io.cpuReq.valid, true)
    poke(cache.io.cpuReq.bits.read, rnd.nextInt(2))
    poke(cache.io.cpuReq.bits.addr, testAddr(i % 100))
    poke(cache.io.cpuReq.bits.data, rnd.nextInt(Int.MaxValue))

    poke(cache.io.memResp.valid, true)
    poke(cache.io.memResp.bits.data, rnd.nextInt(Int.MaxValue))

    while (peek(cache.io.cpuResp.valid) == BigInt(0)) {
      step(1)
    }
    poke(cache.io.cpuResp.valid, false)
    step(1)
  }

  scala.Predef.printf(s"accesses = $accesses\n")

  var readHits = peek(cache.io.readHits)
  var writeHits = peek(cache.io.writeHits)
  var misses = peek(cache.io.readMisses) + peek(cache.io.writeMisses)
  var hitRate = (readHits + writeHits).toDouble / accesses
  var replacements = peek(cache.io.replacements)

  scala.Predef.printf(s"read hits = $readHits\n")
  scala.Predef.printf(s"write hits = $writeHits\n")
  scala.Predef.printf(s"misses = $misses\n")
  scala.Predef.printf(s"hit rate = $hitRate\n")
  scala.Predef.print(s"replacements = $replacements\n")
}

object CacheTester extends App {
  chisel3.iotesters.Driver(() => new Cache) { cache => new CacheTester(cache) }
}
