"""Configuration management for RAG Consultant."""

import os
from dotenv import load_dotenv

load_dotenv()


class Config:
    """Configuration class for RAG Consultant."""
    
    # OpenAI API Key
    OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "")
    
    # Model settings
    EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "text-embedding-ada-002")
    LLM_MODEL = os.getenv("LLM_MODEL", "gpt-3.5-turbo")
    
    # Retrieval settings
    CHUNK_SIZE = int(os.getenv("CHUNK_SIZE", "1000"))
    CHUNK_OVERLAP = int(os.getenv("CHUNK_OVERLAP", "200"))
    TOP_K_RESULTS = int(os.getenv("TOP_K_RESULTS", "4"))
    
    # Paths
    KNOWLEDGE_BASE_PATH = os.getenv("KNOWLEDGE_BASE_PATH", "./data/knowledge_base")
    VECTOR_STORE_PATH = os.getenv("VECTOR_STORE_PATH", "./data/vector_store")
