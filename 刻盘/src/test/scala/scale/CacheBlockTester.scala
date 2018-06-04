package scale

import chisel3._
import chisel3.iotesters.PeekPokeTester

case class CacheBlockEvent(read: Boolean, hit: Boolean, replacement: Boolean)

class CacheBlockTester(block: CacheBlock, events: List[CacheBlockEvent]) extends PeekPokeTester(block) with Params {
  poke(block.io.request.valid, false)
  poke(block.io.request.bits.read, false)
  poke(block.io.request.bits.hit, false)
  poke(block.io.request.bits.replacement, false)
  poke(block.io.response.ready, false)
  step(1)

  events.foreach { event =>
    while (peek(block.io.request.ready) == 0) {
      step(1)
    }

    poke(block.io.request.valid, true)
    poke(block.io.request.bits.read, event.read)
    poke(block.io.request.bits.hit, event.hit)
    poke(block.io.request.bits.replacement, event.replacement)

    poke(block.io.response.ready, true)

    while (peek(block.io.response.valid) == 0) {
      step(1)
    }

    poke(block.io.request.valid, false)

    poke(block.io.response.ready, false)
  }

  step(1)

  val isCounter = peek(block.io.isCounter)
  val siCounter = peek(block.io.siCounter)
  val smCounter = peek(block.io.smCounter)
  val msCounter = peek(block.io.msCounter)
  val imCounter = peek(block.io.imCounter)
  val miCounter = peek(block.io.miCounter)

  scala.Predef.printf(s"isCounter = $isCounter\n")
  scala.Predef.printf(s"siCounter = $siCounter\n")
  scala.Predef.printf(s"smCounter = $smCounter\n")
  scala.Predef.printf(s"msCounter = $msCounter\n")
  scala.Predef.printf(s"imCounter = $imCounter\n")
  scala.Predef.printf(s"miCounter = $miCounter\n")
}

object CacheBlockTester extends App {
  private def test(events: List[CacheBlockEvent]) = {
    chisel3.iotesters.Driver(() => new CacheBlock) { block => new CacheBlockTester(block, events) }
  }

  test(List(
    CacheBlockEvent(read = true, hit = false, replacement = false),
    CacheBlockEvent(read = true, hit = true, replacement = false),
    CacheBlockEvent(read = true, hit = false, replacement = true),
    CacheBlockEvent(read = false, hit = true, replacement = false),
    CacheBlockEvent(read = false, hit = true, replacement = false),
    CacheBlockEvent(read = false, hit = false, replacement = true),
    CacheBlockEvent(read = false, hit = false, replacement = true),
    CacheBlockEvent(read = true, hit = true, replacement = false)
  ))

}
