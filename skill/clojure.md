# Socket REPL Interaction Skill

## Overview

This skill provides expertise in interacting with Clojure socket REPLs for rapid REPL-driven development. Socket REPLs offer incredibly fast startup and connection times, making them ideal for iterative development workflows where quick feedback is essential.

## Core Pattern

Socket REPLs are accessed via `socket-repl-eval`, a babashka script that connects to a socket REPL, sends code, and returns results.

```bash
socket-repl-eval -p 6006 "(+ 1 2)"
```

### How it works

The client sends code over a TCP socket, then does a **half-close** (`shutdownOutput`) — closing only the write side while keeping the read side open. The server reads all forms until EOF, evaluates each, prints results with namespace and timing, then closes the connection.

No `:repl/quit` or other sentinel is needed. Just send code and half-close.

### Output format

Each form's result is printed with namespace and elapsed time:

```
user [0.3ms]=> nil
user [1245.7ms]=> {:count 42}
myapp.core [2.1ms]=> :ok
```

## Tools

### socket-repl-eval

**Usage:**
```bash
socket-repl-eval [OPTIONS] CODE
```

**Options:**
- `-p, --port PORT` — Socket REPL port (default: 6000)
- `-h, --host HOST` — Host address (default: localhost)
- `-t, --timeout SECS` — Timeout in seconds (default: 30)

**Examples:**

Simple evaluation:
```bash
socket-repl-eval -p 6006 "(+ 1 2)"
```

Multiple forms in one request:
```bash
socket-repl-eval -p 6006 "(require 'myapp.core :reload) (myapp.core/process {:x 1})"
```

Running a test:
```bash
socket-repl-eval -p 6006 "(doto 'metabase.tiles.api-test require in-ns) (run-test ad-hoc-query-test)"
```

Reloading and testing:
```bash
socket-repl-eval -p 6006 "(require 'metabase.tiles.api :reload) (doto 'metabase.tiles.api-test require in-ns) (run-tests)"
```

With longer timeout for slow operations:
```bash
socket-repl-eval -p 6006 -t 120 "(run-all-tests)"
```

## Clojure REPL Expertise

### Running Tests

**Run a single test:**
```clojure
(doto 'my.namespace-test require in-ns)
(run-test my-test-name)
```

**Run all tests in a namespace:**
```clojure
(doto 'my.namespace-test require in-ns)
(run-tests)
```

**Run specific namespaces:**
```clojure
(require 'clojure.test)
(clojure.test/run-tests 'my.namespace-test 'other.namespace-test)
```

### Namespace Management

**Require a namespace:**
```clojure
(require 'my.namespace)
```

**Reload after changes:**
```clojure
(require 'my.namespace :reload)
```

**Reload with dependencies:**
```clojure
(require 'my.namespace :reload-all)
```

**Switch to a namespace:**
```clojure
(in-ns 'my.namespace)
```

**Combined require and switch:**
```clojure
(doto 'my.namespace require in-ns)
```

### Getting Information

**Documentation:**
```clojure
(doc function-name)
(doc clojure.test)
```

**Source code:**
```clojure
(source function-name)
```

**Find vars in namespace:**
```clojure
(dir my.namespace)
```

**List all namespaces:**
```clojure
(all-ns)
```

**List vars in current namespace:**
```clojure
(ns-publics *ns*)
```

### Common Workflows

**Edit → Reload → Test:**
1. Edit source file
2. Reload namespace: `(require 'my.namespace :reload)`
3. Switch to test namespace: `(in-ns 'my.namespace-test)`
4. Run tests: `(run-tests)`

**Quick test iteration:**
```clojure
(require 'my.namespace :reload)
(doto 'my.namespace-test require in-ns)
(run-test specific-test)
```

**Multi-namespace reload:**
```clojure
(require 'my.namespace.core :reload)
(require 'my.namespace.api :reload)
(require 'my.namespace.handlers :reload)
(doto 'my.namespace-test require in-ns)
(run-tests)
```

## Best Practices

### 1. Always Reload After Changes
When you've edited a file, always use `:reload` to see your changes:
```clojure
(require 'namespace :reload)
```

### 2. Use `doto` for Convenience
The `doto` pattern is idiomatic for require + in-ns:
```clojure
(doto 'namespace require in-ns)
```

### 3. Run Specific Tests First
When developing, run individual tests for faster feedback:
```clojure
(run-test specific-test-name)
```

### 4. Check Test Output Format
clojure.test returns summary maps:
```clojure
{:test 1, :pass 2, :fail 0, :error 0, :type :summary}
```

### 5. Handle Long-Running Operations
Use the `-t` flag to increase the timeout for slow operations:
```bash
socket-repl-eval -p 6006 -t 120 "(long-running-operation)"
```

## Troubleshooting

### Connection Refused
The socket REPL isn't running. Start the JVM with the socket server flag:
```bash
clj -J"-Dclojure.server.llm-repl={:port 6006 :accept llm-repl.repl/repl}"
```

### Namespace Not Found
Ensure the namespace is on the classpath and use the correct namespace syntax (use `-` not `_`).

### Test Not Found
Make sure you're in the test namespace:
```clojure
(in-ns 'my.namespace-test)
```

### Changes Not Visible
Always use `:reload` when you've edited a file:
```clojure
(require 'namespace :reload)
```

## Integration with Development Workflow

The socket-repl-eval script is designed to work seamlessly with file editing:
1. Use file editing tools to modify Clojure source
2. Use socket-repl-eval to reload and test changes
3. Iterate rapidly without restarting the JVM
4. Add debug printlns and see output immediately

The combination of fast socket REPL connections and Clojure's dynamic nature makes this an extremely productive workflow for LLM-assisted development.
