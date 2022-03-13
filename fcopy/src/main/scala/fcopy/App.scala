package fcopy

import cats.effect._

import java.io.File

object App extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      tuple <- args match {
        case List(source, target) =>
          IO.pure((new File(source), new File(target)))

        case _ =>
          IO.raiseError(new IllegalArgumentException("You should to inform just two files, an source and a target!"))
      }

      count <- copy[IO](tuple._1, tuple._2)
      _ <- IO.println(s"$count bytes has been copied from ${tuple._1.getCanonicalPath} to ${tuple._2.getCanonicalPath}.")
    } yield {
      ExitCode.Success
    }
  }
}
