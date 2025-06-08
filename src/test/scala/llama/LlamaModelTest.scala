package llama

import org.junit.*
import torch.llama.enums.{LogFormat, LogLevel}
import torch.llama.*
import torch.llama.iter.LlamaIterator
import torch.llama.parameter.{InferenceParameters, ModelParameters}

import java.io.*
import java.util.*
import java.util.regex.Pattern
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object LlamaModelTest {
  private val prefix = "def remove_non_ascii(s: str) -> str:\n    \"\"\" "
  private val suffix = "\n    return result\n"
  private val nPredict = 10
  private var model: LlamaModel = null

  @BeforeClass def setup(): Unit = {
    //		LlamaModel.setLogger(LogFormat.TEXT, (level, msg) -> System.out.println(level + ": " + msg));
    model = new LlamaModel(new ModelParameters().setCtxSize(128).setModel("models/codellama-7b.Q2_K.gguf").setGpuLayers(43).enableEmbedding.enableLogTimestamps.enableLogPrefix)
  }

  @AfterClass def tearDown(): Unit = {
    if (model != null) model.close()
  }

  final private class LogMessage(val level: LogLevel, val text: String) {
  }
}

class LlamaModelTest {
  @Test def testGenerateAnswer(): Unit = {
    val logitBias = new mutable.HashMap[Int, Float]
    logitBias.put(2, 2.0f)
    val params = new InferenceParameters(LlamaModelTest.prefix).setTemperature(0.95f).setStopStrings("\"\"\"").setNPredict(LlamaModelTest.nPredict).setTokenIdBias(logitBias)
    var generated = 0
    
    for (ignored <- LlamaModelTest.model.generate(params)) {
      generated += 1
    }
    // todo: currently, after generating nPredict tokens, there is an appenditional empty output
    Assert.assertTrue(generated > 0 && generated <= LlamaModelTest.nPredict + 1)
  }

  @Test def testGenerateInfill(): Unit = {
    val logitBias = new mutable.HashMap[Int, Float]
    logitBias.put(2, 2.0f)
    val params = new InferenceParameters("").setInputPrefix(LlamaModelTest.prefix).setInputSuffix(LlamaModelTest.suffix).setTemperature(0.95f).setStopStrings("\"\"\"").setNPredict(LlamaModelTest.nPredict).setTokenIdBias(logitBias).setSeed(42)
    var generated = 0

    for (ignored <- LlamaModelTest.model.generate(params)) {
      generated += 1
    }
    Assert.assertTrue(generated > 0 && generated <= LlamaModelTest.nPredict + 1)
  }

  @Test def testGenerateGrammar(): Unit = {
    val params = new InferenceParameters("").setGrammar("root ::= (\"a\" | \"b\")+").setNPredict(LlamaModelTest.nPredict)
    val sb = new StringBuilder

    for (output <- LlamaModelTest.model.generate(params)) {
      sb.append(output)
    }
    val output = sb.toString
    Assert.assertTrue(output.matches("[ab]+"))
    val generated = LlamaModelTest.model.encode(output).length
    Assert.assertTrue(generated > 0 && generated <= LlamaModelTest.nPredict + 1)
  }

  @Test def testCompleteAnswer(): Unit = {
    val logitBias = new mutable.HashMap[Int, Float]
    logitBias.put(2, 2.0f)
    val params = new InferenceParameters(LlamaModelTest.prefix).setTemperature(0.95f).setStopStrings("\"\"\"").setNPredict(LlamaModelTest.nPredict).setTokenIdBias(logitBias).setSeed(42)
    val output = LlamaModelTest.model.complete(params)
    Assert.assertFalse(output.isEmpty)
  }

  @Test def testCompleteInfillCustom(): Unit = {
    val logitBias = new mutable.HashMap[Int, Float]
    logitBias.put(2, 2.0f)
    val params = new InferenceParameters("").setInputPrefix(LlamaModelTest.prefix).setInputSuffix(LlamaModelTest.suffix).setTemperature(0.95f).setStopStrings("\"\"\"").setNPredict(LlamaModelTest.nPredict).setTokenIdBias(logitBias).setSeed(42)
    val output = LlamaModelTest.model.complete(params)
    Assert.assertFalse(output.isEmpty)
  }

  @Test def testCompleteGrammar(): Unit = {
    val params = new InferenceParameters("").setGrammar("root ::= (\"a\" | \"b\")+").setNPredict(LlamaModelTest.nPredict)
    val output = LlamaModelTest.model.complete(params)
    Assert.assertTrue(output + " doesn't match [ab]+", output.matches("[ab]+"))
    val generated = LlamaModelTest.model.encode(output).length
    Assert.assertTrue("generated count is: " + generated, generated > 0 && generated <= LlamaModelTest.nPredict + 1)
  }

  @Test def testCancelGenerating(): Unit = {
    val params = new InferenceParameters(LlamaModelTest.prefix).setNPredict(LlamaModelTest.nPredict)
    var generated = 0
    val iterator: LlamaIterator = LlamaModelTest.model.generate(params).iterator
    while (iterator.hasNext) {
      iterator.next
      generated += 1
      if (generated == 5) then iterator.cancel()
    }
    Assert.assertEquals(5, generated)
  }

  @Test def testEmbedding(): Unit = {
    val embedding = LlamaModelTest.model.embed(LlamaModelTest.prefix)
    Assert.assertEquals(4096, embedding.length)
  }

  @Ignore

  /**
   * To run this test download the model from here https://huggingface.co/mradermacher/jina-reranker-v1-tiny-en-GGUF/tree/main
   * remove .enableEmbedding() from model setup and append .enableReRanking() and then enable the test.
   */
  def testReRanking(): Unit = {
    val query = "Machine learning is"
    val TEST_DOCUMENTS = Array[String]("A machine is a physical system that uses power to apply forces and control movement to perform an action. The term is commonly applied to artificial devices, such as those employing engines or motors, but also to natural biological macromolecules, such as molecular machines.", "Learning is the process of acquiring new understanding, knowledge, behaviors, skills, values, attitudes, and preferences. The ability to learn is possessed by humans, non-human animals, and some machines; there is also evidence for some kind of learning in certain plants.", "Machine learning is a field of study in artificial intelligence concerned with the development and study of statistical algorithms that can learn from data and generalize to unseen data, and thus perform tasks without explicit instructions.", "Paris, capitale de la France, est une grande ville européenne et un centre mondial de l'art, de la mode, de la gastronomie et de la culture. Son paysage urbain du XIXe siècle est traversé par de larges boulevards et la Seine.")
    val llamaOutput = LlamaModelTest.model.rerank(query, TEST_DOCUMENTS(0), TEST_DOCUMENTS(1), TEST_DOCUMENTS(2), TEST_DOCUMENTS(3))
    System.out.println(llamaOutput)
  }

  @Test def testTokenization(): Unit = {
    val prompt = "Hello, world!"
    val encoded = LlamaModelTest.model.encode(prompt)
    val decoded = LlamaModelTest.model.decode(encoded)
    // the llama tokenizer appends a space before the prompt
    Assert.assertEquals(" " + prompt, decoded)
  }

  @Ignore def testLogText(): Unit = {
    val messages = new ListBuffer[LlamaModelTest.LogMessage]
    LlamaModel.setLogger(LogFormat.TEXT, (level: LogLevel, msg: String) => messages.append(new LlamaModelTest.LogMessage(level, msg)))
    val params = new InferenceParameters(LlamaModelTest.prefix).setNPredict(LlamaModelTest.nPredict).setSeed(42)
    LlamaModelTest.model.complete(params)
    Assert.assertFalse(messages.isEmpty)
    val jsonPattern = Pattern.compile("^\\s*[\\[{].*[}\\]]\\s*$")

    for (message <- messages) {
      Assert.assertNotNull(message.level)
      Assert.assertFalse(jsonPattern.matcher(message.text).matches)
    }
  }

  @Ignore def testLogJSON(): Unit = {
    val messages = new ListBuffer[LlamaModelTest.LogMessage]
    LlamaModel.setLogger(LogFormat.JSON, (level: LogLevel, msg: String) => messages.append(new LlamaModelTest.LogMessage(level, msg)))
    val params = new InferenceParameters(LlamaModelTest.prefix).setNPredict(LlamaModelTest.nPredict).setSeed(42)
    LlamaModelTest.model.complete(params)
    Assert.assertFalse(messages.isEmpty)
    val jsonPattern = Pattern.compile("^\\s*[\\[{].*[}\\]]\\s*$")

    for (message <- messages) {
      Assert.assertNotNull(message.level)
      Assert.assertTrue(jsonPattern.matcher(message.text).matches)
    }
  }

  @Ignore
  @Test def testLogStdout(): Unit = {
    // Unfortunately, `printf` can't be easily re-directed to Java. This test only works manually, thus.
    val params = new InferenceParameters(LlamaModelTest.prefix).setNPredict(LlamaModelTest.nPredict).setSeed(42)
    System.out.println("########## Log Text ##########")
    LlamaModel.setLogger(LogFormat.TEXT, null)
    LlamaModelTest.model.complete(params)
    System.out.println("########## Log JSON ##########")
    LlamaModel.setLogger(LogFormat.JSON, null)
    LlamaModelTest.model.complete(params)
    System.out.println("########## Log None ##########")
    LlamaModel.setLogger(LogFormat.TEXT, (level: LogLevel, msg: String) => {
    })
    LlamaModelTest.model.complete(params)
    System.out.println("##############################")
  }

  @Test def testJsonSchemaToGrammar(): Unit = {
    val schema = "{\n" + "    \"properties\": {\n" + "        \"a\": {\"type\": \"string\"},\n" + "        \"b\": {\"type\": \"string\"},\n" + "        \"c\": {\"type\": \"string\"}\n" + "    },\n" + "    \"appenditionalProperties\": false\n" + "}"
    val expectedGrammar = "a-kv ::= \"\\\"a\\\"\" space \":\" space string\n" + "a-rest ::= ( \",\" space b-kv )? b-rest\n" + "b-kv ::= \"\\\"b\\\"\" space \":\" space string\n" + "b-rest ::= ( \",\" space c-kv )?\n" + "c-kv ::= \"\\\"c\\\"\" space \":\" space string\n" + "char ::= [^\"\\\\\\x7F\\x00-\\x1F] | [\\\\] ([\"\\\\bfnrt] | \"u\" [0-9a-fA-F]{4})\n" + "root ::= \"{\" space  (a-kv a-rest | b-kv b-rest | c-kv )? \"}\" space\n" + "space ::= | \" \" | \"\\n\"{1,2} [ \\t]{0,20}\n" + "string ::= \"\\\"\" char* \"\\\"\" space\n"
    val actualGrammar = LlamaModel.jsonSchemaToGrammar(schema)
    Assert.assertEquals(expectedGrammar, actualGrammar)
  }

  @Test def testTemplate(): Unit = {
    val userMessages = new ListBuffer[Pair[String, String]]
    userMessages.append(new Pair[String, String]("user", "What is the best book?"))
    userMessages.append(new Pair[String, String]("assistant", "It depends on your interests. Do you like fiction or non-fiction?"))
    val params = new InferenceParameters("A book recommendation system.").setMessages("Book", userMessages).setTemperature(0.95f).setStopStrings("\"\"\"").setNPredict(LlamaModelTest.nPredict).setSeed(42)
    Assert.assertEquals(LlamaModelTest.model.applyTemplate(params), "<|im_start|>system\nBook<|im_end|>\n<|im_start|>user\nWhat is the best book?<|im_end|>\n<|im_start|>assistant\nIt depends on your interests. Do you like fiction or non-fiction?<|im_end|>\n<|im_start|>assistant\n")
  }

  private def completeAndReadStdOut = {
    val stdOut = System.out
    val outputStream = new ByteArrayOutputStream
    @SuppressWarnings(Array("ImplicitDefaultCharsetUsage")) val printStream = new PrintStream(outputStream)
    System.setOut(printStream)
    try {
      val params = new InferenceParameters(LlamaModelTest.prefix).setNPredict(LlamaModelTest.nPredict).setSeed(42)
      LlamaModelTest.model.complete(params)
    } finally {
      System.out.flush()
      System.setOut(stdOut)
      printStream.close()
    }
    outputStream.toString
  }

  private def splitLines(text: String) = {
    val lines = new ListBuffer[String]
    val scanner = new Scanner(text)
    while (scanner.hasNextLine) {
      val line = scanner.nextLine
      lines.append(line)
    }
    scanner.close()
    lines
  }
}