# Storch Llama - Scala 3 Wrapper for Llama.cpp

## Overview
Storch Llama is a Scala 3 project that provides a high-level interface to [llama.cpp](https://github.com/ggerganov/llama.cpp), enabling users to load GGUF files for prediction, inference, and interactive operations. Leveraging the efficiency of the underlying C++ implementation, Storch Llama offers fast inference speeds and supports deployment across multiple operating systems.

## Features
- **GGUF File Support**: Load and utilize GGUF (Llama 2) model files for inference.
- **Fast Inference**: Benefit from the optimized C++ implementation of llama.cpp.
- **Multi-Platform Support**: Deploy on various operating systems, including Linux, macOS, and Windows.
- **Scala 3 Integration**: Enjoy modern Scala features and a type-safe, functional programming experience.

## Getting Started

### Prerequisites
- **Scala 3**: Ensure you have Scala 3 installed on your system. You can download it from [here](https://docs.scala-lang.org/scala3/getting-started.html).
- **C++ Toolchain**: Install a C++ compiler (e.g., GCC on Linux, Clang on macOS, or MSVC on Windows) to build llama.cpp.
- **LLVM**: Required for native code generation. Install it from the official [LLVM website](https://llvm.org/).

### Installation
1. Clone the repository:
```bash
git clone https://github.com/your-repo/storch-llama.cpp.git
cd torch-llama.cpp

```

```` scala 3

sbt compile 

libraryDependencies += "io.github.mullerhai" % "storch-llama-cpp_3" % "0.0.1"

````

```` scala 3 

package example

import torch.llama.iter.LlamaIterable
import torch.llama.{LlamaModel, InferenceParameters}

object InferenceExample extends App {
  // Load the model
  val modelPath = "path/to/your/model.gguf"
  val model = LlamaModel.load(modelPath)

  // Set inference parameters
  val params = InferenceParameters(
    temperature = 0.8,
    topP = 0.95,
    maxTokens = 100
  )

  // Create an iterable for inference
  val iterable = model.generate(params)

  // Iterate over the output
  iterable.foreach(output => {
    print(output.text)
  })
}

````


## Deployment
Storch Llama can be deployed on multiple systems. Here are the general steps:

### Linux
Build the project as described in the Installation section.
Package the application using sbt assembly to create a fat JAR.
Run the JAR with the appropriate Java version:

bash
java -jar your-assembled-jar.jar

### macOS
Follow the same steps as for Linux, ensuring you have the necessary C++ and Java dependencies installed.

### Windows
Install the Visual Studio Build Tools to get the MSVC compiler.
Build the project using SBT.
Run the application using Java:

powershell
Apply
java -jar your-assembled-jar.jar
Future Plans
Enhanced Model Support: Add support for more model formats and architectures.
Distributed Inference: Implement distributed inference capabilities for large-scale deployments.
Improved API: Expand and refine the API to provide more flexibility and functionality.

## Contributing
Contributions are welcome! Please read our CONTRIBUTING.md file for details on how to submit pull requests and report issues.

License
This project is licensed under the MIT License.

