/*--------------------------------------------------------------------------
 *  Copyright 2008 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
package torch.llama

import java.io.IOException
import java.nio.file.{Files, Path, Paths}
import java.util.Locale
import scala.collection.mutable

/**
 * Provides OS name and architecture name.
 *
 * @author leo
 */
@SuppressWarnings(Array("UseOfSystemOutOrSystemErr"))
object OSInfo {
  val X86 = "x86"
  val X64 = "x64"
  val X86_64 = "x86_64"
  val IA64_32 = "ia64_32"
  val IA64 = "ia64"
  val PPC = "ppc"
  val PPC64 = "ppc64"
  private val processRunner = new ProcessRunner
  private val archMapping = new mutable.HashMap[String, String]

  def main(args: Array[String]): Unit = {
    if (args.length >= 1) if ("--os" == args(0)) {
      System.out.print(getOSName)
      return
    }
    else if ("--arch" == args(0)) {
      System.out.print(getArchName)
      return
    }
    System.out.print(getNativeLibFolderPathForCurrentOS)
  }

  private[llama] def getNativeLibFolderPathForCurrentOS = getOSName + "/" + getArchName

  private[llama] def getOSName = translateOSNameToFolderName(System.getProperty("os.name"))

  private[llama] def isAndroid = isAndroidRuntime || isAndroidTermux

  private[llama] def isAndroidRuntime = System.getProperty("java.runtime.name", "").toLowerCase.contains("android")

  private[llama] def isAndroidTermux = try processRunner.runAndWaitFor("uname -o").toLowerCase.contains("android")
  catch {
    case ignored: Exception =>
      false
  }

  private[llama] def isMusl = {
    val mapFilesDir = Paths.get("/proc/self/map_files")
    try {
      val dirStream = Files.list(mapFilesDir)
      try dirStream.map((path: Path) => {
        try path.toRealPath().toString
        catch {
          case e: IOException =>
            ""
        }
      }).anyMatch((s: String) => s.toLowerCase.contains("musl"))
      catch {
        case ignored: Exception =>

          // fall back to checking for alpine linux in the event we're using an older kernel which
          // may not fail the above check
          isAlpineLinux
      } finally if (dirStream != null) dirStream.close()
    }
  }

  private[llama] def isAlpineLinux: Boolean = {
    try {
      val osLines = Files.lines(Paths.get("/etc/os-release"))
      try return osLines.anyMatch((l: String) => l.startsWith("ID") && l.contains("alpine"))
      catch {
        case ignored2: Exception =>
      } finally if (osLines != null) osLines.close()
    }
    false
  }

  private[llama] def getHardwareName = try processRunner.runAndWaitFor("uname -m")
  catch {
    case e: Throwable =>
      System.err.println("Error while running uname -m: " + e.getMessage)
      "unknown"
  }

  private[llama] def resolveArmArchType: String = {
    if (System.getProperty("os.name").contains("Linux")) {
      val armType = getHardwareName
      // armType (uname -m) can be armv5t, armv5te, armv5tej, armv5tejl, armv6, armv7, armv7l,
      // aarch64, i686
      // for Android, we fold everything that is not aarch64 into arm
      if (isAndroid) if (armType.startsWith("aarch64")) {
        // Use arm64
        return "aarch64"
      }
      else return "arm"
      if (armType.startsWith("armv6")) {
        // Raspberry PI
        return "armv6"
      }
      else if (armType.startsWith("armv7")) {
        // Generic
        return "armv7"
      }
      else if (armType.startsWith("armv5")) {
        // Use armv5, soft-float ABI
        return "arm"
      }
      else if (armType.startsWith("aarch64")) {
        // Use arm64
        return "aarch64"
      }
      // Java 1.8 introduces a system property to determine armel or armhf
      // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=8005545
      val abi = System.getProperty("sun.arch.abi")
      if (abi != null && abi.startsWith("gnueabihf")) return "armv7"
      // For java7, we still need to run some shell commands to determine ABI of JVM
      val javaHome = System.getProperty("java.home")
      try {
        // determine if first JVM found uses ARM hard-float ABI
        var exitCode = Runtime.getRuntime.exec("which readelf").waitFor
        if (exitCode == 0) {
          val cmdarray = Array("/bin/sh", "-c", "find '" + javaHome + "' -name 'libjvm.so' | head -1 | xargs readelf -A | " + "grep 'Tag_ABI_VFP_args: VFP registers'")
          exitCode = Runtime.getRuntime.exec(cmdarray).waitFor
          if (exitCode == 0) return "armv7"
        }
        else System.err.println("WARNING! readelf not found. Cannot check if running on an armhf system, armel architecture will be presumed.")
      } catch {
        case e@(_: IOException | _: InterruptedException) =>


        // ignored: fall back to "arm" arch (soft-float ABI)
      }
    }
    // Use armv5, soft-float ABI
    "arm"
  }

  private[llama] def getArchName: String = {
    val `override` = System.getProperty("torch.llama.osinfo.architecture")
    if (`override` != null) return `override`
    var osArch = System.getProperty("os.arch")
    if (osArch.startsWith("arm")) osArch = resolveArmArchType
    else {
      val lc = osArch.toLowerCase(Locale.US)
      if (archMapping.contains(lc)) return archMapping.get(lc).get
    }
    translateArchNameToFolderName(osArch)
  }

  private[llama] def translateOSNameToFolderName(osName: String) = if (osName.contains("Windows")) "Windows"
  else if (osName.contains("Mac") || osName.contains("Darwin")) "Mac"
  else if (osName.contains("AIX")) "AIX"
  else if (isMusl) "Linux-Musl"
  else if (isAndroid) "Linux-Android"
  else if (osName.contains("Linux")) "Linux"
  else osName.replaceAll("\\W", "")

  private[llama] def translateArchNameToFolderName(archName: String) = archName.replaceAll("\\W", "")

  try
    // x86 mappings
    archMapping.put(X86, X86)
  archMapping.put("i386", X86)
  archMapping.put("i486", X86)
  archMapping.put("i586", X86)
  archMapping.put("i686", X86)
  archMapping.put("pentium", X86)
  // x86_64 mappings
  archMapping.put(X86_64, X86_64)
  archMapping.put("amd64", X86_64)
  archMapping.put("em64t", X86_64)
  archMapping.put("universal", X86_64) // Needed for openjdk7 in Mac

  // Itanium 64-bit mappings
  archMapping.put(IA64, IA64)
  archMapping.put("ia64w", IA64)
  // Itanium 32-bit mappings, usually an HP-UX construct
  archMapping.put(IA64_32, IA64_32)
  archMapping.put("ia64n", IA64_32)
  // PowerPC mappings
  archMapping.put(PPC, PPC)
  archMapping.put("power", PPC)
  archMapping.put("powerpc", PPC)
  archMapping.put("power_pc", PPC)
  archMapping.put("power_rs", PPC)
  // TODO: PowerPC 64bit mappings
  archMapping.put(PPC64, PPC64)
  archMapping.put("power64", PPC64)
  archMapping.put("powerpc64", PPC64)
  archMapping.put("power_pc64", PPC64)
  archMapping.put("power_rs64", PPC64)
  archMapping.put("ppc64el", PPC64)
  archMapping.put("ppc64le", PPC64)
  // TODO: Adding X64 support
  archMapping.put(X64, X64)
}