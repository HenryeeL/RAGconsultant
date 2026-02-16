"""RAG Consultant - Core module for document processing and retrieval."""

import os
from typing import List, Optional
from langchain.text_splitter import RecursiveCharacterTextSplitter
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_community.embeddings import OpenAIEmbeddings
from langchain_community.vectorstores import FAISS
from langchain.chains import RetrievalQA
from langchain_community.chat_models import ChatOpenAI
from langchain.prompts import PromptTemplate
from config import Config


class RAGConsultant:
    """Main RAG Consultant class for document retrieval and question answering."""
    
    def __init__(self, api_key: Optional[str] = None):
        """Initialize the RAG Consultant.
        
        Args:
            api_key: OpenAI API key (optional, can be set via environment variable)
        """
        self.api_key = api_key or Config.OPENAI_API_KEY
        if not self.api_key:
            raise ValueError("OpenAI API key is required. Set OPENAI_API_KEY environment variable.")
        
        self.embeddings = OpenAIEmbeddings(openai_api_key=self.api_key)
        self.vector_store = None
        self.qa_chain = None
        
    def load_documents(self, directory_path: str) -> List:
        """Load documents from a directory.
        
        Args:
            directory_path: Path to directory containing documents
            
        Returns:
            List of loaded documents
        """
        if not os.path.exists(directory_path):
            raise FileNotFoundError(f"Directory not found: {directory_path}")
        
        loader = DirectoryLoader(
            directory_path,
            glob="**/*.txt",
            loader_cls=TextLoader,
            show_progress=True
        )
        documents = loader.load()
        return documents
    
    def create_vector_store(self, documents: List) -> None:
        """Create vector store from documents.
        
        Args:
            documents: List of documents to process
        """
        # Split documents into chunks
        text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=Config.CHUNK_SIZE,
            chunk_overlap=Config.CHUNK_OVERLAP
        )
        splits = text_splitter.split_documents(documents)
        
        # Create vector store
        self.vector_store = FAISS.from_documents(splits, self.embeddings)
        
    def save_vector_store(self, path: str) -> None:
        """Save vector store to disk.
        
        Args:
            path: Path to save vector store
        """
        if self.vector_store is None:
            raise ValueError("Vector store not initialized. Call create_vector_store first.")
        
        os.makedirs(os.path.dirname(path), exist_ok=True)
        self.vector_store.save_local(path)
        
    def load_vector_store(self, path: str) -> None:
        """Load vector store from disk.
        
        Args:
            path: Path to load vector store from
        """
        if not os.path.exists(path):
            raise FileNotFoundError(f"Vector store not found: {path}")
        
        self.vector_store = FAISS.load_local(
            path,
            self.embeddings,
            allow_dangerous_deserialization=True
        )
        
    def initialize_qa_chain(self) -> None:
        """Initialize the QA chain with custom prompt."""
        if self.vector_store is None:
            raise ValueError("Vector store not initialized. Load or create vector store first.")
        
        # Create custom prompt template
        template = """You are an intelligent consultant assistant. Use the following pieces of context to answer the question at the end. 
If you don't know the answer based on the context, just say that you don't have enough information to answer, don't try to make up an answer.
Always provide helpful, detailed, and professional responses.

Context:
{context}

Question: {question}

Answer:"""
        
        prompt = PromptTemplate(
            template=template,
            input_variables=["context", "question"]
        )
        
        # Initialize LLM
        llm = ChatOpenAI(
            model_name=Config.LLM_MODEL,
            openai_api_key=self.api_key,
            temperature=0
        )
        
        # Create retrieval QA chain
        self.qa_chain = RetrievalQA.from_chain_type(
            llm=llm,
            chain_type="stuff",
            retriever=self.vector_store.as_retriever(
                search_kwargs={"k": Config.TOP_K_RESULTS}
            ),
            chain_type_kwargs={"prompt": prompt},
            return_source_documents=True
        )
        
    def ask(self, question: str) -> dict:
        """Ask a question to the consultant.
        
        Args:
            question: Question to ask
            
        Returns:
            Dictionary with answer and source documents
        """
        if self.qa_chain is None:
            raise ValueError("QA chain not initialized. Call initialize_qa_chain first.")
        
        result = self.qa_chain({"query": question})
        return {
            "answer": result["result"],
            "sources": result["source_documents"]
        }
