(ns llm-repl.repl
  (:require
   [clojure.main :as m]
   [clojure.test :as t]))

(defn repl
  "Eval server for socket REPL connections.

  Reads forms from the input stream one at a time, evaluates each, and prints
  results with namespace and timing. Exits when the input stream reaches EOF
  (i.e., when the client half-closes the connection).

  Binds test output to *out* so test results appear in the response.
  Starts in the user namespace with standard REPL requires available."
  []
  (binding [*ns* (the-ns 'user)]
    (apply require m/repl-requires)
    (let [eof (Object.)]
      (loop []
        (let [form (read {:read-cond :allow :eof eof} *in*)]
          (when-not (identical? form eof)
            (let [ns-name (ns-name *ns*)
                  t0      (System/nanoTime)
                  result  (binding [t/*test-out* *out*]
                            (eval form))
                  elapsed (/ (- (System/nanoTime) t0) 1e6)]
              (printf "%s [%.1fms]=> " ns-name elapsed)
              (prn result)
              (flush)
              (recur))))))))
