"""CLI interface for RAG Consultant."""

import argparse
import sys
from rag_consultant import RAGConsultant
from config import Config


def setup_consultant():
    """Setup and initialize the RAG Consultant."""
    try:
        consultant = RAGConsultant()
        return consultant
    except ValueError as e:
        print(f"Error: {e}")
        print("\nPlease set your OpenAI API key:")
        print("  export OPENAI_API_KEY='your-api-key-here'")
        sys.exit(1)


def build_knowledge_base(consultant: RAGConsultant, directory: str):
    """Build the knowledge base from documents."""
    print(f"Loading documents from {directory}...")
    try:
        documents = consultant.load_documents(directory)
        print(f"Loaded {len(documents)} documents")
        
        print("Creating vector store...")
        consultant.create_vector_store(documents)
        
        print(f"Saving vector store to {Config.VECTOR_STORE_PATH}...")
        consultant.save_vector_store(Config.VECTOR_STORE_PATH)
        
        print("Knowledge base built successfully!")
    except Exception as e:
        print(f"Error building knowledge base: {e}")
        sys.exit(1)


def interactive_mode(consultant: RAGConsultant):
    """Run the consultant in interactive mode."""
    print("\n" + "="*60)
    print("RAG Consultant - Interactive Mode")
    print("="*60)
    print("Ask me anything! (Type 'quit' or 'exit' to stop)\n")
    
    # Load or build vector store
    try:
        print("Loading knowledge base...")
        consultant.load_vector_store(Config.VECTOR_STORE_PATH)
        print("Knowledge base loaded successfully!")
    except FileNotFoundError:
        print("Knowledge base not found. Building from documents...")
        build_knowledge_base(consultant, Config.KNOWLEDGE_BASE_PATH)
    
    # Initialize QA chain
    consultant.initialize_qa_chain()
    print("Consultant ready!\n")
    
    while True:
        try:
            question = input("You: ").strip()
            
            if question.lower() in ['quit', 'exit', 'q']:
                print("\nGoodbye!")
                break
                
            if not question:
                continue
            
            print("\nConsultant: ", end="", flush=True)
            result = consultant.ask(question)
            print(result["answer"])
            
            # Optionally show sources
            if result["sources"]:
                print(f"\n[Based on {len(result['sources'])} source(s)]")
            print()
            
        except KeyboardInterrupt:
            print("\n\nGoodbye!")
            break
        except Exception as e:
            print(f"\nError: {e}")
            print()


def single_query(consultant: RAGConsultant, question: str):
    """Answer a single question and exit."""
    try:
        consultant.load_vector_store(Config.VECTOR_STORE_PATH)
        consultant.initialize_qa_chain()
        
        result = consultant.ask(question)
        print(result["answer"])
        
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)


def main():
    """Main CLI entry point."""
    parser = argparse.ArgumentParser(
        description="RAG Consultant - Intelligent consultant assistant powered by RAG"
    )
    
    parser.add_argument(
        "--build",
        action="store_true",
        help="Build knowledge base from documents"
    )
    
    parser.add_argument(
        "--directory",
        type=str,
        default=Config.KNOWLEDGE_BASE_PATH,
        help="Directory containing documents (default: ./data/knowledge_base)"
    )
    
    parser.add_argument(
        "--query",
        type=str,
        help="Ask a single question and exit"
    )
    
    args = parser.parse_args()
    
    consultant = setup_consultant()
    
    if args.build:
        build_knowledge_base(consultant, args.directory)
    elif args.query:
        single_query(consultant, args.query)
    else:
        interactive_mode(consultant)


if __name__ == "__main__":
    main()
