package torch.llama

import java.io.{ByteArrayOutputStream, IOException}
import java.util.concurrent.TimeUnit

object ProcessRunner {
  @throws[IOException]
  private def getProcessOutput(process: Process) = try {
    val in = process.getInputStream
    try {
      var readLen = 0
      val b = new ByteArrayOutputStream
      val buf = new Array[Byte](32)
      while ( {
        readLen = in.read(buf, 0, buf.length); readLen >= 0
      }) b.write(buf, 0, readLen)
      b.toString
    } finally if (in != null) in.close()
  }
}

class ProcessRunner {
  @throws[IOException]
  @throws[InterruptedException]
  private[llama] def runAndWaitFor(command: String) = {
    val p = Runtime.getRuntime.exec(command)
    p.waitFor
    ProcessRunner.getProcessOutput(p)
  }

  @throws[IOException]
  @throws[InterruptedException]
  private[llama] def runAndWaitFor(command: String, timeout: Long, unit: TimeUnit) = {
    val p = Runtime.getRuntime.exec(command)
    p.waitFor(timeout, unit)
    ProcessRunner.getProcessOutput(p)
  }
}