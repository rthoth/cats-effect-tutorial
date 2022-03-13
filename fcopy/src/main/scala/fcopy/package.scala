import cats.effect.{IO, Resource}

import java.io.{File, FileInputStream, FileOutputStream}

package object fcopy {

  def copy(source: File, target: File): IO[Long] = {
    createFlow(source, target).use {
      case (input, output) => transfer(input, output)
    }
  }

  private def createFlow(source: File, target: File): Resource[IO, (FileInputStream, FileOutputStream)] = {
    for {
      input <- fileInputStream(source)
      output <- fileOutputStream(target)
    } yield {
      (input, output)
    }
  }

  private def fileInputStream(file: File): Resource[IO, FileInputStream] = {
    Resource.make {
      IO.blocking(new FileInputStream(file))
    } { stream =>
      IO.blocking(stream.close()).handleErrorWith(_ => IO.unit)
    }
  }

  private def fileOutputStream(file: File): Resource[IO, FileOutputStream] = {
    Resource.make {
      IO.blocking(new FileOutputStream(file))
    } { stream =>
      IO.blocking(stream.close()).handleErrorWith(_ => IO.unit)
    }
  }

  private def transfer(input: FileInputStream, output: FileOutputStream): IO[Long] = {
    transfer(input, output, Array.ofDim[Byte](64 * 1024), 0L)
  }

  private def transfer(input: FileInputStream, output: FileOutputStream, buffer: Array[Byte], total: Long): IO[Long] = {
    for {
      amount <- IO.blocking(input.read(buffer))
      count <- if (amount > -1)
        IO.blocking(output.write(buffer)) >> transfer(input, output, buffer, total + amount)
      else
        IO.pure(total)
    } yield {
      count
    }
  }
}
