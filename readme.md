Useful repls and AI connection

Add this repo to your `~/.clojure/deps.edn` file:

```clojure
:llm-repl   {:extra-deps {local/llm-root {:local/root "/Users/dan/projects/clojure/llm-repl"}}}
```

helpful shell scripts:

```shell
# sample usage: clj -J"$(socket-repl 6000)" ... or java "$(socket-repl 6000) -jar ...
socket-repl()
{
 echo -n "-Dclojure.server.repl={:port $1 :accept clojure.core.server/repl}"
}

llm-repl()
{
 echo -n "-Dclojure.server.llm-repl={:port $1 :accept llm-repl.repl/repl}"
}
```

Install these in `~/.zshrc` and add the `socket-repl-eval` script to your bin somewhere and then you can do fun things like:

```
❯ clj -J"$(socket-repl 6001)" -J"$(llm-repl 6002)" -A:llm-repl
Clojure 1.12.1
user=>
```

and from another terminal:

```
❯ socket-repl-eval -p 6001 "(+ 1 1)"
user=> 2
user=> %
```

But even better, the llm repl provides:

```
❯ socket-repl-eval -p 6002 "(+ 1 1)"
user [0.8ms]=> 2
```

Timings and no silly end of file prompts and such.

```shell
❯ socket-repl-eval -p 6002 "\
  (load-file \"/Users/dan/projects/clojure/clojure/test/clojure/test_clojure/clojure_set.clj\")\
  (in-ns 'clojure.test-clojure.clojure-set)\
  (run-tests)"
user [69.0ms]=> #'clojure.test-clojure.clojure-set/test-superset?
user [0.5ms]=> #object[clojure.lang.Namespace 0x7e89d7dd "clojure.test-clojure.clojure-set"]

Testing clojure.test-clojure.clojure-set

Ran 12 tests containing 111 assertions.
0 failures, 0 errors.
clojure.test-clojure.clojure-set [12.1ms]=> {:test 12, :pass 111, :fail 0, :error 0, :type :summary}
```

### Repls:

The repls use different pretty printers and chop down the namespace to just the last segment:

```clojure
user=> (doto 'clojure.set require in-ns) ;; normal repl prints whole namespace
clojure.set
clojure.set=> (llm-repl.human-repl/fipp-repl)
set=> (into clojure.lang.PersistentQueue/EMPTY [1 2])
#clojure.lang.PersistentQueue[1 2]
set=> :repl/quit
nil
clojure.set=> (llm-repl.human-repl/pprint-repl)
set=> (into clojure.lang.PersistentQueue/EMPTY [1 2])
<-(1 2)-<  ;; pprint has some wild choices
set=>
```

#### nocommit-repl

I've often wanted to debug a system while it is in a state that is created by some tests. This state is ephemeral sometimes, by definition transient, and so quick to leave. I wanted some way to pause the world while the state was in effect. But I also wanted some inspectability into the current state. A REPL is a really great tool for this: it's a blocking process that allows for querying the current environment. So here's an example.

Suppose you are making a dynamic programming solution to fibb. You want to investigate some state. You'd love to block the world at a certain point and query current values:

```clojure
set=> (letfn [(fibb-dp [n]
                (peek (reduce (fn [acc x]
                                (when (= x 47) ;; essentially a break point
                                  (llm-repl.human-repl/nocommit-repl))
                                (conj acc (+' (peek acc) (-> acc pop peek))))
                              [0 1]
                              (range n))))]
        (fibb-dp 100))
paused.set=> temp/x   ;; note repl prompt says paused. locals are added to a temp ns
47
paused.set=> (take-last 5 temp/acc)
(701408733 1134903170 1836311903 2971215073 4807526976)
paused.set=> temp/n
100
paused.set=> :repl/quit  ;; resume operation by leaving subrepl
573147844013817084101N
```


## LLM repl:

Starting the process the same way as before:

```shell
❯ clj -J"$(socket-repl 6001)" -J"$(llm-repl 6002)" -A:llm-repl
Clojure 1.12.1
user=>
```


chat with claude:

```
❯ i've got a skill at /Users/dan/projects/clojure/llm-repl/readme.md about how to use the socket
 repl. You've got an llm repl on port 6002. Write a red black tree in clojure, write a few tests
 using the socket repl, and then show me the api and some examples

⏺ Let me start by reading the skill documentation and understanding the setup.

  Read 1 file (ctrl+o to expand)

⏺ Now let me check that socket-repl-eval is available and the REPL is running.

  Searched for 1 pattern (ctrl+o to expand)

⏺ Bash(socket-repl-eval -p 6002 "(+ 1 1)")
  ⎿  user [0.8ms]=> 2

⏺ REPL is live. Let me write the red-black tree implementation.

⏺ Write(src/rbtree.clj)
  ⎿  Wrote 180 lines to src/rbtree.clj
     (ns rbtree)

     ;; Persistent Red-Black Tree (Okasaki-style)
     ;; Nodes: [color left key value right]
     ;; Colors: :red, :black
     ;; Empty tree: nil

     (defn- color [node] (when node (node 0)))
     (defn- left  [node] (when node (node 1)))
     (defn- key-of [node] (when node (node 2)))
     … +170 lines (ctrl+o to expand)

⏺ Now let me load it into the REPL and run some tests.

⏺ Bash(socket-repl-eval -p 6002 "(load-file \"/private/tmp/sorted/src/rbtree.clj\")")
  ⎿  user [67.7ms]=> #'rbtree/height

⏺ Loaded. Now let me run tests.

⏺ Bash(socket-repl-eval -p 6002 "(load-file \"/private/tmp/sorted/test/rbtree_test.clj\") (in-ns
      'rbtree-test) (clojure.test/run-tests)")
  ⎿  user [63.8ms]=> #'rbtree-test/test-height-bound
     user [0.3ms]=> #object[clojure.lang.Namespace 0x4cd4cb2f "rbtree-test"]
     … +5 lines (ctrl+o to expand)

⏺ All 7 tests pass with 24 assertions, 0 failures, 0 errors.

  ---
  API and Examples

  Here's the rbtree namespace API:

  Core Operations

  ┌───────────────┬────────────────────────────┬─────────────────────────────────────────────┐
  │   Function    │         Signature          │                 Description                 │
  ├───────────────┼────────────────────────────┼─────────────────────────────────────────────┤
  │ insert        │ [tree k v]                 │ Insert/update a key-value pair, returns new │
  │               │                            │  tree                                       │
  ├───────────────┼────────────────────────────┼─────────────────────────────────────────────┤
  │ lookup        │ [tree k] / [tree k         │ Find value by key                           │
  │               │ not-found]                 │                                             │
  ├───────────────┼────────────────────────────┼─────────────────────────────────────────────┤
  │ contains-key? │ [tree k]                   │ Check if key exists                         │
  ├───────────────┼────────────────────────────┼─────────────────────────────────────────────┤
  │ size          │ [tree]                     │ Number of entries                           │
  ├───────────────┼────────────────────────────┼─────────────────────────────────────────────┤
  │ height        │ [tree]                     │ Tree height                                 │
  └───────────────┴────────────────────────────┴─────────────────────────────────────────────┘

  Traversal

  ┌──────────────────┬───────────┬──────────────────────────────────┐
  │     Function     │ Signature │           Description            │
  ├──────────────────┼───────────┼──────────────────────────────────┤
  │ tree-seq-ordered │ [tree]    │ Lazy in-order seq of [k v] pairs │
  ├──────────────────┼───────────┼──────────────────────────────────┤
  │ keys-seq         │ [tree]    │ In-order seq of keys             │
  ├──────────────────┼───────────┼──────────────────────────────────┤
  │ vals-seq         │ [tree]    │ In-order seq of values           │
  ├──────────────────┼───────────┼──────────────────────────────────┤
  │ min-key-val      │ [tree]    │ [k v] of minimum key             │
  ├──────────────────┼───────────┼──────────────────────────────────┤
  │ max-key-val      │ [tree]    │ [k v] of maximum key             │
  └──────────────────┴───────────┴──────────────────────────────────┘

  Conversions

  ┌──────────┬───────────┬────────────────────────────┐
  │ Function │ Signature │        Description         │
  ├──────────┼───────────┼────────────────────────────┤
  │ from-map │ [m]       │ Build tree from a map      │
  ├──────────┼───────────┼────────────────────────────┤
  │ ->map    │ [tree]    │ Convert tree to sorted-map │
  └──────────┴───────────┴────────────────────────────┘

  Examples

  ;; Empty tree is nil
  (def t nil)

  ;; Build up a tree
  (def t (-> nil
             (rbtree/insert :cherry 3)
             (rbtree/insert :apple 1)
             (rbtree/insert :banana 2)
             (rbtree/insert :date 4)))

  (rbtree/lookup t :banana)        ;=> 2
  (rbtree/lookup t :fig :nope)     ;=> :nope
  (rbtree/contains-key? t :apple)  ;=> true
  (rbtree/size t)                  ;=> 4

  ;; Sorted traversal
  (rbtree/keys-seq t)              ;=> (:apple :banana :cherry :date)
  (rbtree/tree-seq-ordered t)      ;=> ([:apple 1] [:banana 2] [:cherry 3] [:date 4])

  ;; Min/max
  (rbtree/min-key-val t)           ;=> [:apple 1]
  (rbtree/max-key-val t)           ;=> [:date 4]

  ;; From/to maps
  (rbtree/->map t)                 ;=> {:apple 1, :banana 2, :cherry 3, :date 4}
  (rbtree/from-map {"z" 26 "a" 1 "m" 13})

  ;; Works with any Comparable keys (numbers, strings, keywords...)
  (def nums (reduce (fn [t i] (rbtree/insert t i (* i i)))
                    nil (shuffle (range 10))))
  (rbtree/keys-seq nums)           ;=> (0 1 2 3 4 5 6 7 8 9)
  (rbtree/lookup nums 7)           ;=> 49

  ;; Height stays logarithmic
  (rbtree/height (rbtree/from-map (zipmap (range 1000) (range))))  ;=> ~20

  The tree is persistent (purely functional) — every insert returns a new tree, leaving the old
  one unchanged. Internally it uses Okasaki's balancing approach with nodes represented as
  vectors [color left key value right].

✻ Crunched for 1m 49s
```
