package torch.llama.parameter

import scala.collection.mutable

/**
 * The Java library re-uses most of the llama.cpp server code, which mostly works with JSONs. Thus, the complexity and
 * maintainability is much lower if we work with JSONs. This class provides a simple abstraction to easily create
 * JSON object strings by filling a <code>Map&lt;String, String&gt;</code> with key value pairs.
 */
abstract class JsonParameters {
  // We save parameters directly as a String map here, to re-use as much as possible of the (json-based) C++ code.
  // The JNI code for a proper Java-typed data object is comparatively too complex and hard to maintain.
  final private[llama] val parameters = new mutable.HashMap[String, String]

  override def toString: String = {
    val builder = new StringBuilder
    builder.append("{\n")
    var i = 0

    for (entry <- parameters) {
      val key = entry._1
      val value = entry._2
      builder.append("\t\"").append(key).append("\": ").append(value)
      if ( {
        i += 1;
        i - 1
      } < parameters.size - 1) builder.append(",")
      builder.append("\n")
    }
    builder.append("}")
    builder.toString
  }

  // taken from org.json.JSONObject#quote(String, Writer)
  private[llama] def toJsonString(text: String): String = {
    if (text == null) return null
    val builder = new StringBuilder(text.length + 2)
    var b = 0
    var c = 0
    var hhhh: String = null
    var i = 0
    val len = text.length
    builder.append('"')
    i = 0
    while (i < len) {
      b = c
      c = text.charAt(i)
      c match {
        case '\\' =>
        case '"' =>
          builder.append('\\')
          builder.append(c)
        case '/' =>
          if (b == '<') builder.append('\\')
          builder.append(c)
        case '\b' =>
          builder.append("\\b")
        case '\t' =>
          builder.append("\\t")
        case '\n' =>
          builder.append("\\n")
        case '\f' =>
          builder.append("\\f")
        case '\r' =>
          builder.append("\\r")
        case _ =>
          if (c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
            builder.append("\\u")
            hhhh = Integer.toHexString(c)
            builder.append("0000", 0, 4 - hhhh.length)
            builder.append(hhhh)
          }
          else builder.append(c)
      }
      i += 1
    }
    builder.append('"')
    builder.toString
  }
}