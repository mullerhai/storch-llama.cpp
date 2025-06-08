package torch.llama.parameter

import torch.llama.enums.Sampler.{MIN_P, TEMPERATURE, TOP_K, TOP_P}
import torch.llama.enums.{MiroStat, Sampler}
import torch.llama.{LlamaModel, Pair}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * Parameters used throughout inference of a {@link LlamaModel}, e.g., {@link LlamaModel# generate ( InferenceParameters )}
 * and
 * {@link LlamaModel# complete ( InferenceParameters )}.
 */
@SuppressWarnings(Array("unused"))
object InferenceParameters {
  private val PARAM_PROMPT = "prompt"
  private val PARAM_INPUT_PREFIX = "input_prefix"
  private val PARAM_INPUT_SUFFIX = "input_suffix"
  private val PARAM_CACHE_PROMPT = "cache_prompt"
  private val PARAM_N_PREDICT = "n_predict"
  private val PARAM_TOP_K = "top_k"
  private val PARAM_TOP_P = "top_p"
  private val PARAM_MIN_P = "min_p"
  private val PARAM_TFS_Z = "tfs_z"
  private val PARAM_TYPICAL_P = "typical_p"
  private val PARAM_TEMPERATURE = "temperature"
  private val PARAM_DYNATEMP_RANGE = "dynatemp_range"
  private val PARAM_DYNATEMP_EXPONENT = "dynatemp_exponent"
  private val PARAM_REPEAT_LAST_N = "repeat_last_n"
  private val PARAM_REPEAT_PENALTY = "repeat_penalty"
  private val PARAM_FREQUENCY_PENALTY = "frequency_penalty"
  private val PARAM_PRESENCE_PENALTY = "presence_penalty"
  private val PARAM_MIROSTAT = "mirostat"
  private val PARAM_MIROSTAT_TAU = "mirostat_tau"
  private val PARAM_MIROSTAT_ETA = "mirostat_eta"
  private val PARAM_PENALIZE_NL = "penalize_nl"
  private val PARAM_N_KEEP = "n_keep"
  private val PARAM_SEED = "seed"
  private val PARAM_N_PROBS = "n_probs"
  private val PARAM_MIN_KEEP = "min_keep"
  private val PARAM_GRAMMAR = "grammar"
  private val PARAM_PENALTY_PROMPT = "penalty_prompt"
  private val PARAM_IGNORE_EOS = "ignore_eos"
  private val PARAM_LOGIT_BIAS = "logit_bias"
  private val PARAM_STOP = "stop"
  private val PARAM_SAMPLERS = "samplers"
  private val PARAM_STREAM = "stream"
  private val PARAM_USE_CHAT_TEMPLATE = "use_chat_template"
  private val PARAM_USE_JINJA = "use_jinja"
  private val PARAM_MESSAGES = "messages"
}

@SuppressWarnings(Array("unused"))
final class InferenceParameters(prompt: String) extends JsonParameters {
  // we always need a prompt
  setPrompt(prompt)

  /**
   * Set the prompt to start generation with (default: empty)
   */
  def setPrompt(prompt: String): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_PROMPT, toJsonString(prompt))
    this
  }

  /**
   * Set a prefix for infilling (default: empty)
   */
  def setInputPrefix(inputPrefix: String): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_INPUT_PREFIX, toJsonString(inputPrefix))
    this
  }

  /**
   * Set a suffix for infilling (default: empty)
   */
  def setInputSuffix(inputSuffix: String): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_INPUT_SUFFIX, toJsonString(inputSuffix))
    this
  }

  /**
   * Whether to remember the prompt to avoid reprocessing it
   */
  def setCachePrompt(cachePrompt: Boolean): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_CACHE_PROMPT, String.valueOf(cachePrompt))
    this
  }

  /**
   * Set the number of tokens to predict (default: -1, -1 = infinity, -2 = until context filled)
   */
  def setNPredict(nPredict: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_N_PREDICT, String.valueOf(nPredict))
    this
  }

  /**
   * Set top-k sampling (default: 40, 0 = disabled)
   */
  def setTopK(topK: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_TOP_K, String.valueOf(topK))
    this
  }

  /**
   * Set top-p sampling (default: 0.9, 1.0 = disabled)
   */
  def setTopP(topP: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_TOP_P, String.valueOf(topP))
    this
  }

  /**
   * Set min-p sampling (default: 0.1, 0.0 = disabled)
   */
  def setMinP(minP: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_MIN_P, String.valueOf(minP))
    this
  }

  /**
   * Set tail free sampling, parameter z (default: 1.0, 1.0 = disabled)
   */
  def setTfsZ(tfsZ: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_TFS_Z, String.valueOf(tfsZ))
    this
  }

  /**
   * Set locally typical sampling, parameter p (default: 1.0, 1.0 = disabled)
   */
  def setTypicalP(typicalP: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_TYPICAL_P, String.valueOf(typicalP))
    this
  }

  /**
   * Set the temperature (default: 0.8)
   */
  def setTemperature(temperature: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_TEMPERATURE, String.valueOf(temperature))
    this
  }

  /**
   * Set the dynamic temperature range (default: 0.0, 0.0 = disabled)
   */
  def setDynamicTemperatureRange(dynatempRange: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_DYNATEMP_RANGE, String.valueOf(dynatempRange))
    this
  }

  /**
   * Set the dynamic temperature exponent (default: 1.0)
   */
  def setDynamicTemperatureExponent(dynatempExponent: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_DYNATEMP_EXPONENT, String.valueOf(dynatempExponent))
    this
  }

  /**
   * Set the last n tokens to consider for penalties (default: 64, 0 = disabled, -1 = ctx_size)
   */
  def setRepeatLastN(repeatLastN: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_REPEAT_LAST_N, String.valueOf(repeatLastN))
    this
  }

  /**
   * Set the penalty of repeated sequences of tokens (default: 1.0, 1.0 = disabled)
   */
  def setRepeatPenalty(repeatPenalty: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_REPEAT_PENALTY, String.valueOf(repeatPenalty))
    this
  }

  /**
   * Set the repetition alpha frequency penalty (default: 0.0, 0.0 = disabled)
   */
  def setFrequencyPenalty(frequencyPenalty: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_FREQUENCY_PENALTY, String.valueOf(frequencyPenalty))
    this
  }

  /**
   * Set the repetition alpha presence penalty (default: 0.0, 0.0 = disabled)
   */
  def setPresencePenalty(presencePenalty: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_PRESENCE_PENALTY, String.valueOf(presencePenalty))
    this
  }

  /**
   * Set MiroStat sampling strategies.
   */
  def setMiroStat(mirostat: MiroStat): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_MIROSTAT, String.valueOf(mirostat.ordinal))
    this
  }

  /**
   * Set the MiroStat target entropy, parameter tau (default: 5.0)
   */
  def setMiroStatTau(mirostatTau: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_MIROSTAT_TAU, String.valueOf(mirostatTau))
    this
  }

  /**
   * Set the MiroStat learning rate, parameter eta (default: 0.1)
   */
  def setMiroStatEta(mirostatEta: Float): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_MIROSTAT_ETA, String.valueOf(mirostatEta))
    this
  }

  /**
   * Whether to penalize newline tokens
   */
  def setPenalizeNl(penalizeNl: Boolean): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_PENALIZE_NL, String.valueOf(penalizeNl))
    this
  }

  /**
   * Set the number of tokens to keep from the initial prompt (default: 0, -1 = all)
   */
  def setNKeep(nKeep: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_N_KEEP, String.valueOf(nKeep))
    this
  }

  /**
   * Set the RNG seed (default: -1, use random seed for &lt; 0)
   */
  def setSeed(seed: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_SEED, String.valueOf(seed))
    this
  }

  /**
   * Set the amount top tokens probabilities to output if greater than 0.
   */
  def setNProbs(nProbs: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_N_PROBS, String.valueOf(nProbs))
    this
  }

  /**
   * Set the amount of tokens the samplers should return at least (0 = disabled)
   */
  def setMinKeep(minKeep: Int): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_MIN_KEEP, String.valueOf(minKeep))
    this
  }

  /**
   * Set BNF-like grammar to constrain generations (see samples in grammars/ dir)
   */
  def setGrammar(grammar: String): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_GRAMMAR, toJsonString(grammar))
    this
  }

  /**
   * Override which part of the prompt is penalized for repetition.
   * E.g. if original prompt is "Alice: Hello!" and penaltyPrompt is "Hello!", only the latter will be penalized if
   * repeated. See <a href="https://github.com/ggerganov/llama.cpp/pull/3727">pull request 3727</a> for more details.
   */
  def setPenaltyPrompt(penaltyPrompt: String): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_PENALTY_PROMPT, toJsonString(penaltyPrompt))
    this
  }

  /**
   * Override which tokens to penalize for repetition.
   * E.g. if original prompt is "Alice: Hello!" and penaltyPrompt corresponds to the token ids of "Hello!", only the
   * latter will be penalized if repeated.
   * See <a href="https://github.com/ggerganov/llama.cpp/pull/3727">pull request 3727</a> for more details.
   */
  def setPenaltyPrompt(tokens: Array[Int]): InferenceParameters = {
    if (tokens.length > 0) {
      val builder = new StringBuilder
      builder.append("[")
      for (i <- 0 until tokens.length) {
        builder.append(tokens(i))
        if (i < tokens.length - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_PENALTY_PROMPT, builder.toString)
    }
    this
  }

  /**
   * Set whether to ignore end of stream token and continue generating (implies --logit-bias 2-inf)
   */
  def setIgnoreEos(ignoreEos: Boolean): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_IGNORE_EOS, String.valueOf(ignoreEos))
    this
  }

  /**
   * Modify the likelihood of tokens appearing in the completion by their id. E.g., <code>Map.of(15043, 1f)</code>
   * to increase the  likelihood of token ' Hello', or a negative value to decrease it.
   * Note, this method overrides any previous calls to
   * <ul>
   * <li>{@link # setTokenBias ( Map )}</li>
   * <li>{@link # disableTokens ( Collection )}</li>
   * <li>{@link # disableTokenIds ( Collection )}}</li>
   * </ul>
   */
  def setTokenIdBias(logitBias: mutable.HashMap[Int, Float]): InferenceParameters = {
    if (!logitBias.isEmpty) {
      val builder = new StringBuilder
      builder.append("[")
      var i = 0

      for (entry <- logitBias) {
        val key = entry._1
        val value = entry._2
        builder.append("[").append(key).append(", ").append(value).append("]")
        if ( {
          i += 1;
          i - 1
        } < logitBias.size - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_LOGIT_BIAS, builder.toString)
    }
    this
  }

  /**
   * Set tokens to disable, this corresponds to {@link # setTokenIdBias ( Map )} with a value of
   * {@link Float# NEGATIVE_INFINITY}.
   * Note, this method overrides any previous calls to
   * <ul>
   * <li>{@link # setTokenIdBias ( Map )}</li>
   * <li>{@link # setTokenBias ( Map )}</li>
   * <li>{@link # disableTokens ( Collection )}</li>
   * </ul>
   */
  def disableTokenIds(tokenIds: Seq[Integer]): InferenceParameters = {
    if (!tokenIds.isEmpty) {
      val builder = new StringBuilder
      builder.append("[")
      var i = 0

      for (token <- tokenIds) {
        builder.append("[").append(token).append(", ").append(false).append("]")
        if ( {
          i += 1;
          i - 1
        } < tokenIds.size - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_LOGIT_BIAS, builder.toString)
    }
    this
  }

  /**
   * Modify the likelihood of tokens appearing in the completion by their id. E.g., <code>Map.of(" Hello", 1f)</code>
   * to increase the  likelihood of token id 15043, or a negative value to decrease it.
   * Note, this method overrides any previous calls to
   * <ul>
   * <li>{@link # setTokenIdBias ( Map )}</li>
   * <li>{@link # disableTokens ( Collection )}</li>
   * <li>{@link # disableTokenIds ( Collection )}}</li>
   * </ul>
   */
  def setTokenBias(logitBias: mutable.HashMap[String, Float]): InferenceParameters = {
    if (!logitBias.isEmpty) {
      val builder = new StringBuilder
      builder.append("[")
      var i = 0

      for (entry <- logitBias) {
        val key = entry._1
        val value = entry._2
        builder.append("[").append(toJsonString(key)).append(", ").append(value).append("]")
        if ( {
          i += 1;
          i - 1
        } < logitBias.size - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_LOGIT_BIAS, builder.toString)
    }
    this
  }

  /**
   * Set tokens to disable, this corresponds to {@link # setTokenBias ( Map )} with a value of
   * {@link Float# NEGATIVE_INFINITY}.
   * Note, this method overrides any previous calls to
   * <ul>
   * <li>{@link # setTokenBias ( Map )}</li>
   * <li>{@link # setTokenIdBias ( Map )}</li>
   * <li>{@link # disableTokenIds ( Collection )}</li>
   * </ul>
   */
  def disableTokens(tokens: Seq[String]): InferenceParameters = {
    if (!tokens.isEmpty) {
      val builder = new StringBuilder
      builder.append("[")
      var i = 0

      for (token <- tokens) {
        builder.append("[").append(toJsonString(token)).append(", ").append(false).append("]")
        if ( {
          i += 1;
          i - 1
        } < tokens.size - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_LOGIT_BIAS, builder.toString)
    }
    this
  }

  /**
   * Set strings upon seeing which token generation is stopped
   */
  def setStopStrings(stopStrings: String*): InferenceParameters = {
    if (stopStrings.length > 0) {
      val builder = new StringBuilder
      builder.append("[")
      for (i <- 0 until stopStrings.length) {
        builder.append(toJsonString(stopStrings(i)))
        if (i < stopStrings.length - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_STOP, builder.toString)
    }
    this
  }

  /**
   * Set which samplers to use for token generation in the given order
   */
  def setSamplers(samplers: Sampler*): InferenceParameters = {
    if (samplers.length > 0) {
      val builder = new StringBuilder
      builder.append("[")
      for (i <- 0 until samplers.length) {
        samplers(i) match {
          case TOP_K =>
            builder.append("\"top_k\"")
          case TOP_P =>
            builder.append("\"top_p\"")
          case MIN_P =>
            builder.append("\"min_p\"")
          case TEMPERATURE =>
            builder.append("\"temperature\"")
        }
        if (i < samplers.length - 1) builder.append(", ")
      }
      builder.append("]")
      parameters.put(InferenceParameters.PARAM_SAMPLERS, builder.toString)
    }
    this
  }

  /**
   * Set whether generate should apply a chat template (default: false)
   */
  def setUseChatTemplate(useChatTemplate: Boolean): InferenceParameters = {
    parameters.put(InferenceParameters.PARAM_USE_JINJA, String.valueOf(useChatTemplate))
    this
  }

  /**
   * Set the messages for chat-based inference.
   * - Allows **only one** system message.
   * - Allows **one or more** user/assistant messages.
   */
  def setMessages(systemMessage: String, messages: ListBuffer[Pair[String, String]]): InferenceParameters = {
    val messagesBuilder = new StringBuilder
    messagesBuilder.append("[")
    // Add system message (if provided)
    if (systemMessage != null && !systemMessage.isEmpty) {
      messagesBuilder.append("{\"role\": \"system\", \"content\": ").append(toJsonString(systemMessage)).append("}")
      if (!messages.isEmpty) messagesBuilder.append(", ")
    }
    // Add user/assistant messages
    for (i <- 0 until messages.size) {
      val message = messages(i)
      val role = message.getKey
      val content = message.getValue
      if (!(role == "user") && !(role == "assistant")) throw new IllegalArgumentException("Invalid role: " + role + ". Role must be 'user' or 'assistant'.")
      messagesBuilder.append("{\"role\":").append(toJsonString(role)).append(", \"content\": ").append(toJsonString(content)).append("}")
      if (i < messages.size - 1) messagesBuilder.append(", ")
    }
    messagesBuilder.append("]")
    // Convert ArrayNode to a JSON string and store it in parameters
    parameters.put(InferenceParameters.PARAM_MESSAGES, messagesBuilder.toString)
    this
  }

  private[llama] def setStream(stream: Boolean) = {
    parameters.put(InferenceParameters.PARAM_STREAM, String.valueOf(stream))
    this
  }
}