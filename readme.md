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
  (load-file \"/Users/dan/projects/clojure/clojure/test/clojure/test_clojure/clojure_set.clj\")\   (in-ns 'clojure.test-clojure.clojure-set)\                                                       (run-tests)"                                                                                   user [69.0ms]=> #'clojure.test-clojure.clojure-set/test-superset?
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
