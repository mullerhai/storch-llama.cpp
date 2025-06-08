package torch.llama

import torch.llama.enums.{LogFormat, LogLevel}
import torch.llama.exception.LlamaException
import torch.llama.iter.{LlamaIterable, LlamaIterator}
import torch.llama.parameter.{InferenceParameters, ModelParameters}

import java.lang.annotation.Native
import java.nio.charset.StandardCharsets
import java.util.function.BiConsumer
import scala.annotation.targetName
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * This class is a wrapper around the llama.cpp functionality.
 * Upon being created, it natively allocates memory for the model context.
 * Thus, this class is an {@link AutoCloseable}, in order to de-allocate the memory when it is no longer being needed.
 * <p>
 * The main functionality of this class is:
 * <ul>
 * <li>Streaming answers (and probabilities) via {@link # generate ( InferenceParameters )}</li>
 * <li>Creating whole responses to prompts via {@link # complete ( InferenceParameters )}</li>
 * <li>Creating embeddings via {@link # embed ( String )} (make sure to configure {@link ModelParameters# enableEmbedding ( )}</li>
 * <li>Accessing the tokenizer via {@link # encode ( String )} and {@link # decode ( int [ ] )}</li>
 * </ul>
 */
object LlamaModel {
  /**
   * Sets a callback for native llama.cpp log messages.
   * Per default, log messages are written in JSON to stdout. Note, that in text mode the callback will be also
   * invoked with log messages of the GGML backend, while JSON mode can only access request log messages.
   * In JSON mode, GGML messages will still be written to stdout.
   * To only change the log format but keep logging to stdout, the given callback can be <code>null</code>.
   * To disable logging, pass an empty callback, i.e., <code>(level, msg) -> {}</code>.
   *
   * @param format   the log format to use
   * @param callback a method to call for log messages
   */

  @native def setLogger(format: LogFormat, callback: BiConsumer[LogLevel, String]): Unit

  def jsonSchemaToGrammar(schema: String) = new String(jsonSchemaToGrammarBytes(schema), StandardCharsets.UTF_8)

  @native private def jsonSchemaToGrammarBytes(schema: String): Array[Byte]

  try LlamaLoader.initialize()
}

class LlamaModel(parameters: ModelParameters) extends AutoCloseable {

  /**
   * Load with the given {@link ModelParameters}. Make sure to either set
   * <ul>
   * <li>{@link ModelParameters# setModel ( String )}</li>
   * <li>{@link ModelParameters# setModelUrl ( String )}</li>
   * <li>{@link ModelParameters# setHfRepo ( String )}, {@link ModelParameters# setHfFile ( String )}</li>
   * </ul>
   *
   * @param parameters the set of options
   * @throws LlamaException if no model could be loaded from the given file path
   */

  loadModel(parameters.toArray *)
  @Native private val ctx = 0L

  /**
   * Generate and return a whole answer with custom parameters. Note, that the prompt isn't preprocessed in any
   * way, nothing like "User: ", "###Instruction", etc. is added.
   *
   * @return an LLM response
   */
  def complete(parameters: InferenceParameters): String = {
    parameters.setStream(false)
    val taskId = requestCompletion(parameters.toString)
    val output = receiveCompletion(taskId)
    output.text
  }

  /**
   * Generate and stream outputs with custom inference parameters. Note, that the prompt isn't preprocessed in any
   * way, nothing like "User: ", "###Instruction", etc. is added.
   *
   * @return iterable LLM outputs
   */
  //  def generate(parameters: InferenceParameters): LlamaIterable =  new LlamaIterator(this, parameters)

  def generate(parameters: InferenceParameters): LlamaIterable = {
    new LlamaIterable {
      override def iterator: LlamaIterator = new LlamaIterator(LlamaModel.this, parameters)
    }
  }

  /**
   * Get the embedding of a string. Note, that the prompt isn't preprocessed in any way, nothing like
   * "User: ", "###Instruction", etc. is added.
   *
   * @param prompt the string to embed
   * @return an embedding float array
   * @throws IllegalStateException if embedding mode was not activated (see {@link ModelParameters# enableEmbedding ( )})
   */
  @native def embed(prompt: String): Array[Float]

  /**
   * Tokenize a prompt given the native tokenizer
   *
   * @param prompt the prompt to tokenize
   * @return an array of integers each representing a token id
   */
  @native def encode(prompt: String): Array[Int]

  /**
   * Convert an array of token ids to its string representation
   *
   * @param tokens an array of tokens
   * @return the token ids decoded to a string
   */
  def decode(tokens: Array[Int]): String = {
    val bytes = decodeBytes(tokens)
    new String(bytes, StandardCharsets.UTF_8)
  }

  override def close(): Unit = {
    delete()
  }

  @targetName("rerank")
  def reranks(reRank: Boolean, query: String, documents: String*): ListBuffer[Pair[String, Float]] = {
    val output = rerank(query, documents *)
    val scoredDocumentMap = output.probabilities
    val rankedDocuments = new ListBuffer[Pair[String, Float]]
    if (reRank) {
      // Sort in descending order based on Float values
      scoredDocumentMap.toSeq.sortBy(_._2).reverse.foreach { case (key, value) => rankedDocuments.append(new Pair[String, Float](key, value)) }
      //.sorted((a: mutable.HashMap.Entry[String, Float], b: mutable.HashMap.Entry[String, Float]) => Float.compare(b.getValue, a.getValue)) // Descending order.forEach((entry: mutable.HashMap.Entry[String, Float]) => rankedDocuments.add(new Pair[String, Float](entry.getKey, entry.getValue)))
    }
    else {
      // Copy without sorting
      scoredDocumentMap.foreach((key: String, value: Float) => rankedDocuments.append(new Pair[String, Float](key, value)))
    }
    rankedDocuments
  }

  @native def rerank(query: String, documents: String*): LlamaOutput

  def applyTemplate(parameters: InferenceParameters): String = applyTemplate(parameters.toString)

  @native def applyTemplate(parametersJson: String): String

  // don't overload native methods since the C++ function names get nasty
  @native
  @throws[LlamaException]
  private[llama] def requestCompletion(params: String): Int

  @native
  @throws[LlamaException]
  private[llama] def receiveCompletion(taskId: Int): LlamaOutput

  @native private[llama] def cancelCompletion(taskId: Int): Unit

  @native private[llama] def decodeBytes(tokens: Array[Int]): Array[Byte]

  @native
  @throws[LlamaException]
  private def loadModel(parameters: String*): Unit

  @native private def delete(): Unit

  @native private[llama] def releaseTask(taskId: Int): Unit
}