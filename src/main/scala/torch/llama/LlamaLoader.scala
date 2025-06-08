/*--------------------------------------------------------------------------
 *  Copyright 2007 Taro L. Saito
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

import java.io.{BufferedInputStream, File, IOException, InputStream}
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks.{break, breakable}

/**
 * Set the system properties, torch.llama.lib.path, torch.llama.lib.name, appropriately so that the
 * library can find *.dll, *.dylib and *.so files, according to the current OS (win, linux, mac).
 *
 * <p>The library files are automatically extracted from this project's package (JAR).
 *
 * <p>usage: call {@link # initialize ( )} before using the library.
 *
 * @author leo
 */
class LlamaLoader

@SuppressWarnings(Array("UseOfSystemOutOrSystemErr"))
object LlamaLoader {
  private var extracted = false

  /**
   * Loads the llama and jllama shared libraries
   */
  @throws[UnsatisfiedLinkError]
  private[llama] def initialize(): Unit = {
    // only cleanup before the first extract
    if (!extracted) cleanup()
    if ("Mac" == OSInfo.getOSName) {
      val nativeDirName = getNativeResourcePath
      val tempFolder = getTempDir.getAbsolutePath
      System.out.println(nativeDirName)
      val metalFilePath = extractFile(nativeDirName, "ggml-metal.metal", tempFolder, false)
      if (metalFilePath == null) System.err.println("'ggml-metal.metal' not found")
    }
    loadNativeLibrary("jllama")
    extracted = true
  }

  /**
   * Deleted old native libraries e.g. on Windows the DLL file is not removed on VM-Exit (bug #80)
   */
  private def cleanup(): Unit = {
    try {
      val dirList = Files.list(getTempDir.toPath)
      try dirList.filter(LlamaLoader.shouldCleanPath).forEach(LlamaLoader.cleanPath)
      catch {
        case e: IOException =>
          System.err.println("Failed to open directory: " + e.getMessage)
      } finally if (dirList != null) dirList.close()
    }
  }

  private def shouldCleanPath(path: Path) = {
    val fileName = path.getFileName.toString
    fileName.startsWith("jllama") || fileName.startsWith("torch/llama")
  }

  private def cleanPath(path: Path): Unit = {
    try Files.delete(path)
    catch {
      case e: Exception =>
        System.err.println("Failed to delete old native lib: " + e.getMessage)
    }
  }

  private def loadNativeLibrary(name: String): Unit = {
    val triedPaths = new ListBuffer[String]
    val nativeLibName = System.mapLibraryName(name)
    var nativeLibPath = System.getProperty("torch.llama.lib.path")
    if (nativeLibPath != null) {
      val path = Paths.get(nativeLibPath, nativeLibName)
      if (loadNativeLibrary(path)) return
      else triedPaths.append(nativeLibPath)
    }
    if (OSInfo.isAndroid) try {
      // loadLibrary can load directly from packed apk file automatically
      // if java-llama.cpp is added as code source
      System.loadLibrary(name)
      return
    } catch {
      case e: UnsatisfiedLinkError =>
        triedPaths.append("Directly from .apk/lib")
    }
    // Try to load the library from java.library.path
    val javaLibraryPath = System.getProperty("java.library.path", "")
    for (ldPath <- javaLibraryPath.split(File.pathSeparator)) {
      breakable {
        if (ldPath.isEmpty) then break
      }
      val path = Paths.get(ldPath, nativeLibName)
      if (loadNativeLibrary(path)) return
      else triedPaths.append(ldPath)
    }
    // As a last resort try load the os-dependent library from the jar file
    nativeLibPath = getNativeResourcePath
    if (hasNativeLib(nativeLibPath, nativeLibName)) {
      // temporary library folder
      val tempFolder = getTempDir.getAbsolutePath
      // Try extracting the library from jar
      if (extractAndLoadLibraryFile(nativeLibPath, nativeLibName, tempFolder)) return
      else triedPaths.append(nativeLibPath)
    }
    throw new UnsatisfiedLinkError(String.format("No native library found for os.name=%s, os.arch=%s, paths=[%s]", OSInfo.getOSName, OSInfo.getArchName, triedPaths.mkString(File.pathSeparator)))
  }

  /**
   * Loads native library using the given path and name of the library
   *
   * @param path path of the native library
   * @return true for successfully loading, otherwise false
   */
  def loadNativeLibrary(path: Path): Boolean = {
    if (!Files.exists(path)) return false
    val absolutePath = path.toAbsolutePath.toString
    try {
      System.load(absolutePath)
      true
    } catch {
      case e: UnsatisfiedLinkError =>
        System.err.println(e.getMessage)
        System.err.println("Failed to load native library: " + absolutePath + ". osinfo: " + OSInfo.getNativeLibFolderPathForCurrentOS)
        false
    }
  }

  /**
   * Extracts and loads the specified library file to the target folder
   *
   * @param libFolderForCurrentOS Library path.
   * @param libraryFileName       Library name.
   * @param targetFolder          Target folder.
   * @return whether the library was successfully loaded
   */
  private def extractAndLoadLibraryFile(libFolderForCurrentOS: String, libraryFileName: String, targetFolder: String): Boolean = {
    val path = extractFile(libFolderForCurrentOS, libraryFileName, targetFolder, true)
    if (path == null) return false
    loadNativeLibrary(path)
  }

  //  @Nullable 
  private def extractFile(sourceDirectory: String, fileName: String, targetDirectory: String, addUuid: Boolean): Path = {
    val nativeLibraryFilePath = sourceDirectory + "/" + fileName
    val extractedFilePath = Paths.get(targetDirectory, fileName)
    try {
      // Extract a native library file into the target directory
      try {
        val reader = classOf[LlamaLoader].getResourceAsStream(nativeLibraryFilePath)
        try {
          if (reader == null) return null
          Files.copy(reader, extractedFilePath, StandardCopyOption.REPLACE_EXISTING)
        } finally {

          // Delete the extracted lib file on JVM exit.
          extractedFilePath.toFile.deleteOnExit()
          if (reader != null) reader.close()
        }
      }
      // Set executable (x) flag to enable Java to load the native library
      extractedFilePath.toFile.setReadable(true)
      extractedFilePath.toFile.setWritable(true, true)
      extractedFilePath.toFile.setExecutable(true)
      // Check whether the contents are properly copied from the resource folder
      try {
        val nativeIn = classOf[LlamaLoader].getResourceAsStream(nativeLibraryFilePath)
        val extractedLibIn = Files.newInputStream(extractedFilePath)
        try if (!contentsEquals(nativeIn, extractedLibIn)) throw new RuntimeException(String.format("Failed to write a native library file at %s", extractedFilePath))
        finally {
          if (nativeIn != null) nativeIn.close()
          if (extractedLibIn != null) extractedLibIn.close()
        }
      }
      System.out.println("Extracted '" + fileName + "' to '" + extractedFilePath + "'")
      extractedFilePath
    } catch {
      case e: IOException =>
        System.err.println(e.getMessage)
        null
    }
  }

  @throws[IOException]
  private def contentsEquals(inputStream1: InputStream, inputStream2: InputStream): Boolean = {
    var in1 = inputStream1
    var in2 = inputStream2
    if (!in1.isInstanceOf[BufferedInputStream]) then in1 = new BufferedInputStream(in1)
    if (!in2.isInstanceOf[BufferedInputStream]) then in2 = new BufferedInputStream(in2)
    var ch = in1.read
    while (ch != -1) {
      val ch2 = in2.read
      if (ch != ch2) return false
      ch = in1.read
    }
    val ch2 = in2.read
    ch2 == -1
  }

  private def getTempDir = new File(System.getProperty("torch.llama.tmpdir", System.getProperty("java.io.tmpdir")))

  private def getNativeResourcePath = {
    val packagePath = classOf[LlamaLoader].getPackage.getName.replace(".", "/")
    String.format("/%s/%s", packagePath, OSInfo.getNativeLibFolderPathForCurrentOS)
  }

  private def hasNativeLib(path: String, libraryName: String) = classOf[LlamaLoader].getResource(path + "/" + libraryName) != null
}