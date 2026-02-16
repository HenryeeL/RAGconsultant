# RAG Consultant

An intelligent consultant assistant powered by Retrieval-Augmented Generation (RAG) technology.

## Overview

RAG Consultant combines the power of large language models with a custom knowledge base to provide accurate, context-aware consulting assistance. It uses vector embeddings and semantic search to retrieve relevant information and generate informed responses.

## Features

- **Document-based Knowledge**: Load and index your own documents as a knowledge base
- **Semantic Search**: Uses embeddings to find the most relevant information
- **Intelligent Responses**: Powered by OpenAI's GPT models
- **Interactive CLI**: Easy-to-use command-line interface
- **Source Attribution**: Tracks which documents informed each answer

## Installation

1. Clone the repository:
```bash
git clone https://github.com/HenryeeL/RAGconsultant.git
cd RAGconsultant
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Set up your OpenAI API key:
```bash
cp .env.example .env
# Edit .env and add your OpenAI API key
```

Or export it directly:
```bash
export OPENAI_API_KEY='your-api-key-here'
```

## Usage

### Interactive Mode (Default)

Start the consultant in interactive mode:
```bash
python main.py
```

The first time you run it, it will automatically build the knowledge base from documents in `./data/knowledge_base/`.

### Build Knowledge Base

Manually build/rebuild the knowledge base:
```bash
python main.py --build
```

Use a custom directory:
```bash
python main.py --build --directory /path/to/your/documents
```

### Single Query

Ask a single question and exit:
```bash
python main.py --query "What is RAG technology?"
```

## Adding Your Own Documents

1. Add `.txt` files to the `./data/knowledge_base/` directory
2. Rebuild the knowledge base:
```bash
python main.py --build
```

## Configuration

Edit `.env` to customize:

- `OPENAI_API_KEY`: Your OpenAI API key (required)
- `EMBEDDING_MODEL`: Embedding model (default: text-embedding-ada-002)
- `LLM_MODEL`: Language model (default: gpt-3.5-turbo)
- `CHUNK_SIZE`: Document chunk size (default: 1000)
- `CHUNK_OVERLAP`: Chunk overlap (default: 200)
- `TOP_K_RESULTS`: Number of documents to retrieve (default: 4)

## Architecture

The RAG Consultant uses the following components:

1. **Document Loader**: Loads text documents from the knowledge base directory
2. **Text Splitter**: Breaks documents into chunks for better retrieval
3. **Embeddings**: Converts text to vector representations using OpenAI embeddings
4. **Vector Store**: FAISS-based vector database for efficient similarity search
5. **Retrieval Chain**: Retrieves relevant chunks and generates responses using GPT

## Example

```bash
$ python main.py

Loading knowledge base...
Knowledge base loaded successfully!
Consultant ready!

You: What is RAG technology?

Consultant: Retrieval-Augmented Generation (RAG) is a powerful AI technique 
that combines information retrieval with text generation. RAG systems work by 
retrieving relevant documents from a knowledge base and using them as context 
for a language model to generate responses...

[Based on 4 source(s)]

You: quit
Goodbye!
```

## Requirements

- Python 3.8+
- OpenAI API key
- See `requirements.txt` for Python dependencies

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.
