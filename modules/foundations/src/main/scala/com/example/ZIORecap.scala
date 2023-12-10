package com.example

import zio.*

object ZIORecap extends ZIOAppDefault:
  // suspension or delay
  val aSuspension: IO[Throwable, Int] = ZIO.suspend(ZIO.succeed(10))

  // error handling
  val anAttempt: IO[Throwable, Int] = ZIO.attempt {
    // Exp which can throw exception
    val str: String = null
    str.length
  }

  anAttempt.catchAll(e => ZIO.succeed(s"Error message: $e"))

  val fiber = ZIO.sleep(1.second) *> Random.nextIntBetween(0, 10)
  val delayedNumber = for {
    fib1 <- fiber.fork
    fib2 <- fiber.fork
    a <- fib1.join
    b <- fib2.join
  } yield (a, b)

  val interruptable = for {
    fib <- delayedNumber.onInterrupt(ZIO.succeed(println(s"I am interrupted"))).fork
    _   <- ZIO.succeed(println("Cancelling fiber")).delay(500.millis) *> fib.interrupt
    _   <- fib.join
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] = interruptable
//    Console.printLine("Hello ZIO")
//    aSuspension.flatMap(n => Console.printLine(s"aSuspend $n"))
//    delayedNumber.flatMap(t => Console.printLine(s"ZIO Fork: $t"))
