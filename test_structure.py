"""Unit tests for RAG Consultant - Code structure validation."""

import ast
import sys


def test_rag_consultant_class():
    """Test that rag_consultant.py has the correct class structure."""
    print("Testing RAGConsultant class structure...")
    
    with open("rag_consultant.py", "r") as f:
        tree = ast.parse(f.read())
    
    # Find RAGConsultant class
    classes = [node for node in ast.walk(tree) if isinstance(node, ast.ClassDef)]
    rag_class = None
    for cls in classes:
        if cls.name == "RAGConsultant":
            rag_class = cls
            break
    
    if not rag_class:
        print("✗ RAGConsultant class not found")
        return False
    
    print("✓ RAGConsultant class found")
    
    # Check for required methods
    methods = [node.name for node in ast.walk(rag_class) if isinstance(node, ast.FunctionDef)]
    required_methods = [
        "__init__",
        "load_documents",
        "create_vector_store",
        "save_vector_store",
        "load_vector_store",
        "initialize_qa_chain",
        "ask"
    ]
    
    for method in required_methods:
        if method in methods:
            print(f"  ✓ Method '{method}' found")
        else:
            print(f"  ✗ Method '{method}' missing")
            return False
    
    return True


def test_config_class():
    """Test that config.py has the correct structure."""
    print("\nTesting Config class structure...")
    
    with open("config.py", "r") as f:
        tree = ast.parse(f.read())
    
    # Find Config class
    classes = [node for node in ast.walk(tree) if isinstance(node, ast.ClassDef)]
    config_class = None
    for cls in classes:
        if cls.name == "Config":
            config_class = cls
            break
    
    if not config_class:
        print("✗ Config class not found")
        return False
    
    print("✓ Config class found")
    
    # Check for required attributes (assignments in class body)
    assignments = [node.targets[0].id for node in ast.walk(config_class) 
                  if isinstance(node, ast.Assign) and isinstance(node.targets[0], ast.Name)]
    
    required_attrs = [
        "OPENAI_API_KEY",
        "EMBEDDING_MODEL",
        "LLM_MODEL",
        "CHUNK_SIZE",
        "CHUNK_OVERLAP",
        "TOP_K_RESULTS",
        "KNOWLEDGE_BASE_PATH",
        "VECTOR_STORE_PATH"
    ]
    
    for attr in required_attrs:
        if attr in assignments:
            print(f"  ✓ Attribute '{attr}' found")
        else:
            print(f"  ✗ Attribute '{attr}' missing")
            return False
    
    return True


def test_main_functions():
    """Test that main.py has the correct function structure."""
    print("\nTesting main.py function structure...")
    
    with open("main.py", "r") as f:
        tree = ast.parse(f.read())
    
    # Find all functions
    functions = [node.name for node in ast.walk(tree) if isinstance(node, ast.FunctionDef)]
    
    required_functions = [
        "setup_consultant",
        "build_knowledge_base",
        "interactive_mode",
        "single_query",
        "main"
    ]
    
    for func in required_functions:
        if func in functions:
            print(f"  ✓ Function '{func}' found")
        else:
            print(f"  ✗ Function '{func}' missing")
            return False
    
    return True


def test_docstrings():
    """Test that main modules have docstrings."""
    print("\nTesting module docstrings...")
    
    files = {
        "rag_consultant.py": "RAG Consultant",
        "config.py": "Configuration",
        "main.py": "CLI"
    }
    
    for file, expected_content in files.items():
        with open(file, "r") as f:
            tree = ast.parse(f.read())
        
        docstring = ast.get_docstring(tree)
        if docstring and expected_content.lower() in docstring.lower():
            print(f"  ✓ {file} has proper docstring")
        else:
            print(f"  ✗ {file} missing or inadequate docstring")
            return False
    
    return True


def main():
    """Run all structure tests."""
    print("="*60)
    print("RAG Consultant - Code Structure Tests")
    print("="*60 + "\n")
    
    tests = [
        ("RAGConsultant Class", test_rag_consultant_class),
        ("Config Class", test_config_class),
        ("Main Functions", test_main_functions),
        ("Module Docstrings", test_docstrings)
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
