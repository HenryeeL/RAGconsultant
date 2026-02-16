# Quick Start Guide

## Installation

```bash
# Install dependencies
pip install -r requirements.txt

# Configure OpenAI API key
export OPENAI_API_KEY='your-api-key-here'
```

## Basic Usage

### Interactive Mode
```bash
python main.py
```

### Build Knowledge Base
```bash
python main.py --build
```

### Single Query
```bash
python main.py --query "What is RAG?"
```

## Project Structure

```
RAGconsultant/
├── config.py              # Configuration management
├── rag_consultant.py      # Core RAG implementation
├── main.py                # CLI interface
├── requirements.txt       # Python dependencies
├── .env.example          # Example environment variables
├── .gitignore            # Git ignore patterns
├── test_basic.py         # Basic validation tests
├── test_structure.py     # Code structure tests
└── data/
    └── knowledge_base/   # Document storage
        ├── rag_technology.txt
        ├── consulting_practices.txt
        └── ai_ml_basics.txt
```

## Key Features

1. **Document Loading**: Automatically loads `.txt` files from the knowledge base
2. **Vector Embeddings**: Uses OpenAI embeddings for semantic search
3. **FAISS Vector Store**: Efficient similarity search
4. **GPT Integration**: Generates responses using GPT-3.5-turbo
5. **Source Attribution**: Tracks which documents informed each answer

## Configuration Options

Edit `.env` or set environment variables:

- `OPENAI_API_KEY`: Your OpenAI API key (required)
- `EMBEDDING_MODEL`: text-embedding-ada-002
- `LLM_MODEL`: gpt-3.5-turbo
- `CHUNK_SIZE`: 1000
- `CHUNK_OVERLAP`: 200
- `TOP_K_RESULTS`: 4

## Testing

```bash
# Run basic tests
python test_basic.py

# Run structure tests
python test_structure.py
```

## Adding Your Documents

1. Place `.txt` files in `data/knowledge_base/`
2. Run: `python main.py --build`
3. Start querying: `python main.py`
