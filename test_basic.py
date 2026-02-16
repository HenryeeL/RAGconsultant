"""Basic tests for RAG Consultant functionality."""

import os
import sys
import tempfile
import shutil


def test_imports():
    """Test that all modules can be imported."""
    print("Testing imports...")
    try:
        import config
        print("✓ config module imported")
        
        # Check Config class
        assert hasattr(config.Config, 'OPENAI_API_KEY')
        assert hasattr(config.Config, 'EMBEDDING_MODEL')
        assert hasattr(config.Config, 'LLM_MODEL')
        print("✓ Config class has required attributes")
        
    except ImportError as e:
        print(f"✗ Import error: {e}")
        return False
    except AssertionError as e:
        print(f"✗ Assertion error: {e}")
        return False
    
    return True


def test_file_structure():
    """Test that all required files exist."""
    print("\nTesting file structure...")
    required_files = [
        "config.py",
        "main.py",
        "rag_consultant.py",
        "requirements.txt",
        ".gitignore",
        ".env.example",
        "README.md"
    ]
    
    required_dirs = [
        "data/knowledge_base"
    ]
    
    for file in required_files:
        if os.path.exists(file):
            print(f"✓ {file} exists")
        else:
            print(f"✗ {file} missing")
            return False
    
    for dir in required_dirs:
        if os.path.isdir(dir):
            print(f"✓ {dir}/ exists")
        else:
            print(f"✗ {dir}/ missing")
            return False
    
    return True


def test_knowledge_base_documents():
    """Test that example documents exist."""
    print("\nTesting knowledge base documents...")
    kb_path = "data/knowledge_base"
    
    if not os.path.exists(kb_path):
        print(f"✗ Knowledge base directory missing")
        return False
    
    txt_files = [f for f in os.listdir(kb_path) if f.endswith('.txt')]
    
    if len(txt_files) == 0:
        print(f"✗ No .txt files found in knowledge base")
        return False
    
    print(f"✓ Found {len(txt_files)} document(s) in knowledge base:")
    for file in txt_files:
        file_path = os.path.join(kb_path, file)
        size = os.path.getsize(file_path)
        print(f"  - {file} ({size} bytes)")
    
    return True


def test_requirements():
    """Test that requirements.txt has necessary packages."""
    print("\nTesting requirements...")
    
    with open("requirements.txt", "r") as f:
        content = f.read()
    
    required_packages = [
        "langchain",
        "openai",
        "faiss-cpu",
        "python-dotenv"
    ]
    
    for package in required_packages:
        if package in content:
            print(f"✓ {package} in requirements.txt")
        else:
            print(f"✗ {package} missing from requirements.txt")
            return False
    
    return True


def test_gitignore():
    """Test that .gitignore has important patterns."""
    print("\nTesting .gitignore...")
    
    with open(".gitignore", "r") as f:
        content = f.read()
    
    required_patterns = [
        ("__pycache__", "__pycache__"),
        ("*.pyc or *.py[cod]", ["*.pyc", "*.py[cod]"]),
        (".env", ".env"),
        ("venv", "venv"),
        ("*.faiss", "*.faiss")
    ]
    
    for description, patterns in required_patterns:
        if isinstance(patterns, list):
            found = any(pattern in content for pattern in patterns)
        else:
            found = patterns in content
        
        if found:
            print(f"✓ {description} in .gitignore")
        else:
            print(f"✗ {description} missing from .gitignore")
            return False
    
    return True


def test_readme():
    """Test that README has essential sections."""
    print("\nTesting README...")
    
    with open("README.md", "r") as f:
        content = f.read()
    
    required_sections = [
        "Installation",
        "Usage",
        "Configuration",
        "RAG"
    ]
    
    for section in required_sections:
        if section.lower() in content.lower():
            print(f"✓ '{section}' section found in README")
        else:
            print(f"✗ '{section}' section missing from README")
            return False
    
    return True


def main():
    """Run all tests."""
    print("="*60)
    print("RAG Consultant - Basic Tests")
    print("="*60)
    
    tests = [
        ("File Structure", test_file_structure),
        ("Imports", test_imports),
        ("Knowledge Base Documents", test_knowledge_base_documents),
        ("Requirements", test_requirements),
        ("GitIgnore", test_gitignore),
        ("README", test_readme)
    ]
    
    results = []
    for name, test_func in tests:
        try:
            result = test_func()
            results.append((name, result))
        except Exception as e:
            print(f"\n✗ {name} test failed with exception: {e}")
            import traceback
            traceback.print_exc()
            results.append((name, False))
    
    print("\n" + "="*60)
    print("Test Results")
    print("="*60)
    
    passed = 0
    failed = 0
    for name, result in results:
        status = "PASSED" if result else "FAILED"
        symbol = "✓" if result else "✗"
        print(f"{symbol} {name}: {status}")
        if result:
            passed += 1
        else:
            failed += 1
    
    print(f"\nTotal: {passed} passed, {failed} failed")
    
    return failed == 0


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
