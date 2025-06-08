package torch.llama.parameter

import torch.llama.LlamaModel
import torch.llama.enums.*
import torch.llama.parameter.CliParameters

import scala.collection.mutable

/** *
 * Parameters used for initializing a {@link LlamaModel}.
 */
@SuppressWarnings(Array("unused"))
final class ModelParameters extends CliParameters {
  /**
   * Set the number of threads to use during generation (default: -1).
   */
  def setThreads(nThreads: Int): ModelParameters = {
    parameters.put("--threads", String.valueOf(nThreads))
    this
  }

  /**
   * Set the number of threads to use during batch and prompt processing (default: same as --threads).
   */
  def setThreadsBatch(nThreads: Int): ModelParameters = {
    parameters.put("--threads-batch", String.valueOf(nThreads))
    this
  }

  /**
   * Set the CPU affinity mask: arbitrarily long hex. Complements cpu-range (default: "").
   */
  def setCpuMask(mask: String): ModelParameters = {
    parameters.put("--cpu-mask", mask)
    this
  }

  /**
   * Set the range of CPUs for affinity. Complements --cpu-mask.
   */
  def setCpuRange(range: String): ModelParameters = {
    parameters.put("--cpu-range", range)
    this
  }

  /**
   * Use strict CPU placement (default: 0).
   */
  def setCpuStrict(strictCpu: Int): ModelParameters = {
    parameters.put("--cpu-strict", String.valueOf(strictCpu))
    this
  }

  /**
   * Set process/thread priority: 0-normal, 1-medium, 2-high, 3-realtime (default: 0).
   */
  def setPriority(priority: Int): ModelParameters = {
    if (priority < 0 || priority > 3) throw new IllegalArgumentException("Invalid value for priority")
    parameters.put("--prio", String.valueOf(priority))
    this
  }

  /**
   * Set the polling level to wait for work (0 - no polling, default: 0).
   */
  def setPoll(poll: Int): ModelParameters = {
    parameters.put("--poll", String.valueOf(poll))
    this
  }

  /**
   * Set the CPU affinity mask for batch processing: arbitrarily long hex. Complements cpu-range-batch (default: same as --cpu-mask).
   */
  def setCpuMaskBatch(mask: String): ModelParameters = {
    parameters.put("--cpu-mask-batch", mask)
    this
  }

  /**
   * Set the ranges of CPUs for batch affinity. Complements --cpu-mask-batch.
   */
  def setCpuRangeBatch(range: String): ModelParameters = {
    parameters.put("--cpu-range-batch", range)
    this
  }

  /**
   * Use strict CPU placement for batch processing (default: same as --cpu-strict).
   */
  def setCpuStrictBatch(strictCpuBatch: Int): ModelParameters = {
    parameters.put("--cpu-strict-batch", String.valueOf(strictCpuBatch))
    this
  }

  /**
   * Set process/thread priority for batch processing: 0-normal, 1-medium, 2-high, 3-realtime (default: 0).
   */
  def setPriorityBatch(priorityBatch: Int): ModelParameters = {
    if (priorityBatch < 0 || priorityBatch > 3) throw new IllegalArgumentException("Invalid value for priority batch")
    parameters.put("--prio-batch", String.valueOf(priorityBatch))
    this
  }

  /**
   * Set the polling level for batch processing (default: same as --poll).
   */
  def setPollBatch(pollBatch: Int): ModelParameters = {
    parameters.put("--poll-batch", String.valueOf(pollBatch))
    this
  }

  /**
   * Set the size of the prompt context (default: 0, 0 = loaded from model).
   */
  def setCtxSize(ctxSize: Int): ModelParameters = {
    parameters.put("--ctx-size", String.valueOf(ctxSize))
    this
  }

  /**
   * Set the number of tokens to predict (default: -1 = infinity, -2 = until context filled).
   */
  def setPredict(nPredict: Int): ModelParameters = {
    parameters.put("--predict", String.valueOf(nPredict))
    this
  }

  /**
   * Set the logical maximum batch size (default: 0).
   */
  def setBatchSize(batchSize: Int): ModelParameters = {
    parameters.put("--batch-size", String.valueOf(batchSize))
    this
  }

  /**
   * Set the physical maximum batch size (default: 0).
   */
  def setUbatchSize(ubatchSize: Int): ModelParameters = {
    parameters.put("--ubatch-size", String.valueOf(ubatchSize))
    this
  }

  /**
   * Set the number of tokens to keep from the initial prompt (default: -1 = all).
   */
  def setKeep(keep: Int): ModelParameters = {
    parameters.put("--keep", String.valueOf(keep))
    this
  }

  /**
   * Disable context shift on infinite text generation (default: enabled).
   */
  def disableContextShift: ModelParameters = {
    parameters.put("--no-context-shift", null)
    this
  }

  /**
   * Enable Flash Attention (default: disabled).
   */
  def enableFlashAttn: ModelParameters = {
    parameters.put("--flash-attn", null)
    this
  }

  /**
   * Disable internal libllama performance timings (default: false).
   */
  def disablePerf: ModelParameters = {
    parameters.put("--no-perf", null)
    this
  }

  /**
   * Process escape sequences (default: true).
   */
  def enableEscape: ModelParameters = {
    parameters.put("--escape", null)
    this
  }

  /**
   * Do not process escape sequences (default: false).
   */
  def disableEscape: ModelParameters = {
    parameters.put("--no-escape", null)
    this
  }

  /**
   * Enable special tokens output (default: true).
   */
  def enableSpecial: ModelParameters = {
    parameters.put("--special", null)
    this
  }

  /**
   * Skip warming up the model with an empty run (default: false).
   */
  def skipWarmup: ModelParameters = {
    parameters.put("--no-warmup", null)
    this
  }

  /**
   * Use Suffix/Prefix/Middle pattern for infill (instead of Prefix/Suffix/Middle) as some models prefer this.
   * (default: disabled)
   */
  def setSpmInfill: ModelParameters = {
    parameters.put("--spm-infill", null)
    this
  }

  /**
   * Set samplers that will be used for generation in the order, separated by ';' (default: all).
   */
  def setSamplers(samplers: Sampler*): ModelParameters = {
    if (samplers.length > 0) {
      val builder = new StringBuilder
      for (i <- 0 until samplers.length) {
        val sampler = samplers(i)
        builder.append(sampler.toString.toLowerCase)
        if (i < samplers.length - 1) builder.append(";")
      }
      parameters.put("--samplers", builder.toString)
    }
    this
  }

  /**
   * Set RNG seed (default: -1, use random seed).
   */
  def setSeed(seed: Long): ModelParameters = {
    parameters.put("--seed", String.valueOf(seed))
    this
  }

  /**
   * Ignore end of stream token and continue generating (implies --logit-bias EOS-inf).
   */
  def ignoreEos: ModelParameters = {
    parameters.put("--ignore-eos", null)
    this
  }

  /**
   * Set temperature for sampling (default: 0.8).
   */
  def setTemp(temp: Float): ModelParameters = {
    parameters.put("--temp", String.valueOf(temp))
    this
  }

  /**
   * Set top-k sampling (default: 40, 0 = disabled).
   */
  def setTopK(topK: Int): ModelParameters = {
    parameters.put("--top-k", String.valueOf(topK))
    this
  }

  /**
   * Set top-p sampling (default: 0.95, 1.0 = disabled).
   */
  def setTopP(topP: Float): ModelParameters = {
    parameters.put("--top-p", String.valueOf(topP))
    this
  }

  /**
   * Set min-p sampling (default: 0.05, 0.0 = disabled).
   */
  def setMinP(minP: Float): ModelParameters = {
    parameters.put("--min-p", String.valueOf(minP))
    this
  }

  /**
   * Set xtc probability (default: 0.0, 0.0 = disabled).
   */
  def setXtcProbability(xtcProbability: Float): ModelParameters = {
    parameters.put("--xtc-probability", String.valueOf(xtcProbability))
    this
  }

  /**
   * Set xtc threshold (default: 0.1, 1.0 = disabled).
   */
  def setXtcThreshold(xtcThreshold: Float): ModelParameters = {
    parameters.put("--xtc-threshold", String.valueOf(xtcThreshold))
    this
  }

  /**
   * Set locally typical sampling parameter p (default: 1.0, 1.0 = disabled).
   */
  def setTypical(typP: Float): ModelParameters = {
    parameters.put("--typical", String.valueOf(typP))
    this
  }

  /**
   * Set last n tokens to consider for penalize (default: 64, 0 = disabled, -1 = ctx_size).
   */
  def setRepeatLastN(repeatLastN: Int): ModelParameters = {
    if (repeatLastN < -1) throw new RuntimeException("Invalid repeat-last-n value")
    parameters.put("--repeat-last-n", String.valueOf(repeatLastN))
    this
  }

  /**
   * Set penalize repeat sequence of tokens (default: 1.0, 1.0 = disabled).
   */
  def setRepeatPenalty(repeatPenalty: Float): ModelParameters = {
    parameters.put("--repeat-penalty", String.valueOf(repeatPenalty))
    this
  }

  /**
   * Set repeat alpha presence penalty (default: 0.0, 0.0 = disabled).
   */
  def setPresencePenalty(presencePenalty: Float): ModelParameters = {
    parameters.put("--presence-penalty", String.valueOf(presencePenalty))
    this
  }

  /**
   * Set repeat alpha frequency penalty (default: 0.0, 0.0 = disabled).
   */
  def setFrequencyPenalty(frequencyPenalty: Float): ModelParameters = {
    parameters.put("--frequency-penalty", String.valueOf(frequencyPenalty))
    this
  }

  /**
   * Set DRY sampling multiplier (default: 0.0, 0.0 = disabled).
   */
  def setDryMultiplier(dryMultiplier: Float): ModelParameters = {
    parameters.put("--dry-multiplier", String.valueOf(dryMultiplier))
    this
  }

  /**
   * Set DRY sampling base value (default: 1.75).
   */
  def setDryBase(dryBase: Float): ModelParameters = {
    parameters.put("--dry-base", String.valueOf(dryBase))
    this
  }

  /**
   * Set allowed length for DRY sampling (default: 2).
   */
  def setDryAllowedLength(dryAllowedLength: Int): ModelParameters = {
    parameters.put("--dry-allowed-length", String.valueOf(dryAllowedLength))
    this
  }

  /**
   * Set DRY penalty for the last n tokens (default: -1, 0 = disable, -1 = context size).
   */
  def setDryPenaltyLastN(dryPenaltyLastN: Int): ModelParameters = {
    if (dryPenaltyLastN < -1) throw new RuntimeException("Invalid dry-penalty-last-n value")
    parameters.put("--dry-penalty-last-n", String.valueOf(dryPenaltyLastN))
    this
  }

  /**
   * Add sequence breaker for DRY sampling, clearing out default breakers (default: none).
   */
  def setDrySequenceBreaker(drySequenceBreaker: String): ModelParameters = {
    parameters.put("--dry-sequence-breaker", drySequenceBreaker)
    this
  }

  /**
   * Set dynamic temperature range (default: 0.0, 0.0 = disabled).
   */
  def setDynatempRange(dynatempRange: Float): ModelParameters = {
    parameters.put("--dynatemp-range", String.valueOf(dynatempRange))
    this
  }

  /**
   * Set dynamic temperature exponent (default: 1.0).
   */
  def setDynatempExponent(dynatempExponent: Float): ModelParameters = {
    parameters.put("--dynatemp-exp", String.valueOf(dynatempExponent))
    this
  }

  /**
   * Use Mirostat sampling (default: PLACEHOLDER, 0 = disabled, 1 = Mirostat, 2 = Mirostat 2.0).
   */
  def setMirostat(mirostat: MiroStat): ModelParameters = {
    parameters.put("--mirostat", String.valueOf(mirostat.ordinal))
    this
  }

  /**
   * Set Mirostat learning rate, parameter eta (default: 0.1).
   */
  def setMirostatLR(mirostatLR: Float): ModelParameters = {
    parameters.put("--mirostat-lr", String.valueOf(mirostatLR))
    this
  }

  /**
   * Set Mirostat target entropy, parameter tau (default: 5.0).
   */
  def setMirostatEnt(mirostatEnt: Float): ModelParameters = {
    parameters.put("--mirostat-ent", String.valueOf(mirostatEnt))
    this
  }

  /**
   * Modify the likelihood of token appearing in the completion.
   */
  def setLogitBias(tokenIdAndBias: String): ModelParameters = {
    parameters.put("--logit-bias", tokenIdAndBias)
    this
  }

  /**
   * Set BNF-like grammar to constrain generations (default: empty).
   */
  def setGrammar(grammar: String): ModelParameters = {
    parameters.put("--grammar", grammar)
    this
  }

  /**
   * Specify the file to read grammar from.
   */
  def setGrammarFile(fileName: String): ModelParameters = {
    parameters.put("--grammar-file", fileName)
    this
  }

  /**
   * Specify the JSON schema to constrain generations (default: empty).
   */
  def setJsonSchema(schema: String): ModelParameters = {
    parameters.put("--json-schema", schema)
    this
  }

  /**
   * Set pooling type for embeddings (default: model default if unspecified).
   */
  def setPoolingType(dtype: PoolingType): ModelParameters = {
    parameters.put("--pooling", String.valueOf(dtype.getId))
    this
  }

  /**
   * Set RoPE frequency scaling method (default: linear unless specified by the model).
   */
  def setRopeScaling(dtype: RopeScalingType): ModelParameters = {
    parameters.put("--rope-scaling", String.valueOf(dtype.getId))
    this
  }

  /**
   * Set RoPE context scaling factor, expands context by a factor of N.
   */
  def setRopeScale(ropeScale: Float): ModelParameters = {
    parameters.put("--rope-scale", String.valueOf(ropeScale))
    this
  }

  /**
   * Set RoPE base frequency, used by NTK-aware scaling (default: loaded from model).
   */
  def setRopeFreqBase(ropeFreqBase: Float): ModelParameters = {
    parameters.put("--rope-freq-base", String.valueOf(ropeFreqBase))
    this
  }

  /**
   * Set RoPE frequency scaling factor, expands context by a factor of 1/N.
   */
  def setRopeFreqScale(ropeFreqScale: Float): ModelParameters = {
    parameters.put("--rope-freq-scale", String.valueOf(ropeFreqScale))
    this
  }

  /**
   * Set YaRN: original context size of model (default: model training context size).
   */
  def setYarnOrigCtx(yarnOrigCtx: Int): ModelParameters = {
    parameters.put("--yarn-orig-ctx", String.valueOf(yarnOrigCtx))
    this
  }

  /**
   * Set YaRN: extrapolation mix factor (default: 0.0 = full interpolation).
   */
  def setYarnExtFactor(yarnExtFactor: Float): ModelParameters = {
    parameters.put("--yarn-ext-factor", String.valueOf(yarnExtFactor))
    this
  }

  /**
   * Set YaRN: scale sqrt(t) or attention magnitude (default: 1.0).
   */
  def setYarnAttnFactor(yarnAttnFactor: Float): ModelParameters = {
    parameters.put("--yarn-attn-factor", String.valueOf(yarnAttnFactor))
    this
  }

  /**
   * Set YaRN: high correction dim or alpha (default: 1.0).
   */
  def setYarnBetaSlow(yarnBetaSlow: Float): ModelParameters = {
    parameters.put("--yarn-beta-slow", String.valueOf(yarnBetaSlow))
    this
  }

  /**
   * Set YaRN: low correction dim or beta (default: 32.0).
   */
  def setYarnBetaFast(yarnBetaFast: Float): ModelParameters = {
    parameters.put("--yarn-beta-fast", String.valueOf(yarnBetaFast))
    this
  }

  /**
   * Set group-attention factor (default: 1).
   */
  def setGrpAttnN(grpAttnN: Int): ModelParameters = {
    parameters.put("--grp-attn-n", String.valueOf(grpAttnN))
    this
  }

  /**
   * Set group-attention width (default: 512).
   */
  def setGrpAttnW(grpAttnW: Int): ModelParameters = {
    parameters.put("--grp-attn-w", String.valueOf(grpAttnW))
    this
  }

  /**
   * Enable verbose printing of the KV cache.
   */
  def enableDumpKvCache: ModelParameters = {
    parameters.put("--dump-kv-cache", null)
    this
  }

  /**
   * Disable KV offload.
   */
  def disableKvOffload: ModelParameters = {
    parameters.put("--no-kv-offload", null)
    this
  }

  /**
   * Set KV cache data type for K (allowed values: F16).
   */
  def setCacheTypeK(dtype: CacheType): ModelParameters = {
    parameters.put("--cache-type-k", dtype.toString.toLowerCase)
    this
  }

  /**
   * Set KV cache data type for V (allowed values: F16).
   */
  def setCacheTypeV(dtype: CacheType): ModelParameters = {
    parameters.put("--cache-type-v", dtype.toString.toLowerCase)
    this
  }

  /**
   * Set KV cache defragmentation threshold (default: 0.1, &lt; 0 - disabled).
   */
  def setDefragThold(defragThold: Float): ModelParameters = {
    parameters.put("--defrag-thold", String.valueOf(defragThold))
    this
  }

  /**
   * Set the number of parallel sequences to decode (default: 1).
   */
  def setParallel(nParallel: Int): ModelParameters = {
    parameters.put("--parallel", String.valueOf(nParallel))
    this
  }

  /**
   * Enable continuous batching (a.k.a dynamic batching) (default: disabled).
   */
  def enableContBatching: ModelParameters = {
    parameters.put("--cont-batching", null)
    this
  }

  /**
   * Disable continuous batching.
   */
  def disableContBatching: ModelParameters = {
    parameters.put("--no-cont-batching", null)
    this
  }

  /**
   * Force system to keep model in RAM rather than swapping or compressing.
   */
  def enableMlock: ModelParameters = {
    parameters.put("--mlock", null)
    this
  }

  /**
   * Do not memory-map model (slower load but may reduce pageouts if not using mlock).
   */
  def disableMmap: ModelParameters = {
    parameters.put("--no-mmap", null)
    this
  }

  /**
   * Set NUMA optimization type for system.
   */
  def setNuma(numaStrategy: NumaStrategy): ModelParameters = {
    parameters.put("--numa", numaStrategy.toString.toLowerCase)
    this
  }

  /**
   * Set comma-separated list of devices to use for offloading &lt;dev1,dev2,..&gt; (none = don't offload).
   */
  def setDevices(devices: String): ModelParameters = {
    parameters.put("--device", devices)
    this
  }

  /**
   * Set the number of layers to store in VRAM.
   */
  def setGpuLayers(gpuLayers: Int): ModelParameters = {
    parameters.put("--gpu-layers", String.valueOf(gpuLayers))
    this
  }

  /**
   * Set how to split the model across multiple GPUs (none, layer, row).
   */
  def setSplitMode(splitMode: GpuSplitMode): ModelParameters = {
    parameters.put("--split-mode", splitMode.toString.toLowerCase)
    this
  }

  /**
   * Set fraction of the model to offload to each GPU, comma-separated list of proportions N0,N1,N2,....
   */
  def setTensorSplit(tensorSplit: String): ModelParameters = {
    parameters.put("--tensor-split", tensorSplit)
    this
  }

  /**
   * Set the GPU to use for the model (with split-mode = none), or for intermediate results and KV (with split-mode = row).
   */
  def setMainGpu(mainGpu: Int): ModelParameters = {
    parameters.put("--main-gpu", String.valueOf(mainGpu))
    this
  }

  /**
   * Enable checking model tensor data for invalid values.
   */
  def enableCheckTensors: ModelParameters = {
    parameters.put("--check-tensors", null)
    this
  }

  /**
   * Override model metadata by key. This option can be specified multiple times.
   */
  def setOverrideKv(keyValue: String): ModelParameters = {
    parameters.put("--override-kv", keyValue)
    this
  }

  /**
   * Add a LoRA adapter (can be repeated to use multiple adapters).
   */
  def addLoraAdapter(fname: String): ModelParameters = {
    parameters.put("--lora", fname)
    this
  }

  /**
   * Add a LoRA adapter with user-defined scaling (can be repeated to use multiple adapters).
   */
  def addLoraScaledAdapter(fname: String, scale: Float): ModelParameters = {
    parameters.put("--lora-scaled", fname + "," + scale)
    this
  }

  /**
   * Add a control vector (this argument can be repeated to add multiple control vectors).
   */
  def addControlVector(fname: String): ModelParameters = {
    parameters.put("--control-vector", fname)
    this
  }

  /**
   * Add a control vector with user-defined scaling (can be repeated to add multiple scaled control vectors).
   */
  def addControlVectorScaled(fname: String, scale: Float): ModelParameters = {
    parameters.put("--control-vector-scaled", fname + "," + scale)
    this
  }

  /**
   * Set the layer range to apply the control vector(s) to (start and end inclusive).
   */
  def setControlVectorLayerRange(start: Int, end: Int): ModelParameters = {
    parameters.put("--control-vector-layer-range", start + "," + end)
    this
  }

  /**
   * Set the model path from which to load the base model.
   */
  def setModel(model: String): ModelParameters = {
    parameters.put("--model", model)
    this
  }

  /**
   * Set the model download URL (default: unused).
   */
  def setModelUrl(modelUrl: String): ModelParameters = {
    parameters.put("--model-url", modelUrl)
    this
  }

  /**
   * Set the Hugging Face model repository (default: unused).
   */
  def setHfRepo(hfRepo: String): ModelParameters = {
    parameters.put("--hf-repo", hfRepo)
    this
  }

  /**
   * Set the Hugging Face model file (default: unused).
   */
  def setHfFile(hfFile: String): ModelParameters = {
    parameters.put("--hf-file", hfFile)
    this
  }

  /**
   * Set the Hugging Face model repository for the vocoder model (default: unused).
   */
  def setHfRepoV(hfRepoV: String): ModelParameters = {
    parameters.put("--hf-repo-v", hfRepoV)
    this
  }

  /**
   * Set the Hugging Face model file for the vocoder model (default: unused).
   */
  def setHfFileV(hfFileV: String): ModelParameters = {
    parameters.put("--hf-file-v", hfFileV)
    this
  }

  /**
   * Set the Hugging Face access token (default: value from HF_TOKEN environment variable).
   */
  def setHfToken(hfToken: String): ModelParameters = {
    parameters.put("--hf-token", hfToken)
    this
  }

  /**
   * Enable embedding use case; use only with dedicated embedding models.
   */
  def enableEmbedding: ModelParameters = {
    parameters.put("--embedding", null)
    this
  }

  /**
   * Enable reranking endpoint on server.
   */
  def enableReranking: ModelParameters = {
    parameters.put("--reranking", null)
    this
  }

  /**
   * Set minimum chunk size to attempt reusing from the cache via KV shifting.
   */
  def setCacheReuse(cacheReuse: Int): ModelParameters = {
    parameters.put("--cache-reuse", String.valueOf(cacheReuse))
    this
  }

  /**
   * Set the path to save the slot kv cache.
   */
  def setSlotSavePath(slotSavePath: String): ModelParameters = {
    parameters.put("--slot-save-path", slotSavePath)
    this
  }

  /**
   * Set custom jinja chat template.
   */
  def setChatTemplate(chatTemplate: String): ModelParameters = {
    parameters.put("--chat-template", chatTemplate)
    this
  }

  /**
   * Set how much the prompt of a request must match the prompt of a slot in order to use that slot.
   */
  def setSlotPromptSimilarity(similarity: Float): ModelParameters = {
    parameters.put("--slot-prompt-similarity", String.valueOf(similarity))
    this
  }

  /**
   * Load LoRA adapters without applying them (apply later via POST /lora-adapters).
   */
  def setLoraInitWithoutApply: ModelParameters = {
    parameters.put("--lora-init-without-apply", null)
    this
  }

  /**
   * Disable logging.
   */
  def disableLog: ModelParameters = {
    parameters.put("--log-disable", null)
    this
  }

  /**
   * Set the log file path.
   */
  def setLogFile(logFile: String): ModelParameters = {
    parameters.put("--log-file", logFile)
    this
  }

  /**
   * Set verbosity level to infinity (log all messages, useful for debugging).
   */
  def setVerbose: ModelParameters = {
    parameters.put("--verbose", null)
    this
  }

  /**
   * Set the verbosity threshold (messages with a higher verbosity will be ignored).
   */
  def setLogVerbosity(verbosity: Int): ModelParameters = {
    parameters.put("--log-verbosity", String.valueOf(verbosity))
    this
  }

  /**
   * Enable prefix in log messages.
   */
  def enableLogPrefix: ModelParameters = {
    parameters.put("--log-prefix", null)
    this
  }

  /**
   * Enable timestamps in log messages.
   */
  def enableLogTimestamps: ModelParameters = {
    parameters.put("--log-timestamps", null)
    this
  }

  /**
   * Set the number of tokens to draft for speculative decoding.
   */
  def setDraftMax(draftMax: Int): ModelParameters = {
    parameters.put("--draft-max", String.valueOf(draftMax))
    this
  }

  /**
   * Set the minimum number of draft tokens to use for speculative decoding.
   */
  def setDraftMin(draftMin: Int): ModelParameters = {
    parameters.put("--draft-min", String.valueOf(draftMin))
    this
  }

  /**
   * Set the minimum speculative decoding probability for greedy decoding.
   */
  def setDraftPMin(draftPMin: Float): ModelParameters = {
    parameters.put("--draft-p-min", String.valueOf(draftPMin))
    this
  }

  /**
   * Set the size of the prompt context for the draft model.
   */
  def setCtxSizeDraft(ctxSizeDraft: Int): ModelParameters = {
    parameters.put("--ctx-size-draft", String.valueOf(ctxSizeDraft))
    this
  }

  /**
   * Set the comma-separated list of devices to use for offloading the draft model.
   */
  def setDeviceDraft(deviceDraft: String): ModelParameters = {
    parameters.put("--device-draft", deviceDraft)
    this
  }

  /**
   * Set the number of layers to store in VRAM for the draft model.
   */
  def setGpuLayersDraft(gpuLayersDraft: Int): ModelParameters = {
    parameters.put("--gpu-layers-draft", String.valueOf(gpuLayersDraft))
    this
  }

  /**
   * Set the draft model for speculative decoding.
   */
  def setModelDraft(modelDraft: String): ModelParameters = {
    parameters.put("--model-draft", modelDraft)
    this
  }

  /**
   * Enable jinja for templating
   */
  def enableJinja: ModelParameters = {
    parameters.put("--jinja", null)
    this
  }
}