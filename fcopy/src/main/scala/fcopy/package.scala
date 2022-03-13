import cats.effect.{Resource, Sync}
import cats.syntax.all._

import java.io.{File, FileInputStream, FileOutputStream, InputStream, OutputStream}

package object fcopy {

  def copy[F[_] : Sync](source: File, target: File): F[Long] = {
    openChannel(source, target).use {
      case (input, output) => transfer(input, output)
    }
  }

  private def fileInputStream[F[_] : Sync](file: File): Resource[F, InputStream] = {
    val sync = Sync[F]
    Resource.make {
      sync.blocking(new FileInputStream(file))
    } { stream =>
      sync.blocking(stream.close()).handleErrorWith(_ => sync.unit)
    }
  }

  private def fileOutputStream[F[_] : Sync](file: File): Resource[F, OutputStream] = {
    val sync = Sync[F]
    Resource.make {
      sync.blocking(new FileOutputStream(file))
    } { stream =>
      sync.blocking(stream.close()).handleErrorWith(_ => sync.unit)
    }
  }

  private def openChannel[F[_] : Sync](source: File, target: File): Resource[F, (InputStream, OutputStream)] = {
    for {
      input <- fileInputStream(source)
      output <- fileOutputStream(target)
    } yield {
      (input, output)
    }
  }

  private def transfer[F[_] : Sync](input: InputStream, output: OutputStream): F[Long] = {
    transfer(input, output, Array.ofDim[Byte](64 * 1024), 0L)
  }

  private def transfer[F[_] : Sync](input: InputStream, output: OutputStream,
    buffer: Array[Byte], total: Long): F[Long] = {

    val sync = Sync[F]
    for {
      amount <- sync.blocking(input.read(buffer))
      count <- if (amount > -1)
        sync.blocking(output.write(buffer)) >> transfer(input, output, buffer, total + amount)
      else
        sync.pure(total)
    } yield {
      count
    }
  }
}
