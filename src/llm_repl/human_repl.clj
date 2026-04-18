(ns llm-repl.human-repl
  (:require [clojure.main]
            [clojure.string :as str]
            [clojure.core.server :as server]
            [clojure.pprint :as pprint]
            clojure.test
            [fipp.edn :as fipp]))

(defn pprint-repl
  "The normal repl using pprint"
  []
  (clojure.main/repl
    :prompt (fn [] (printf "%s=> " (peek (str/split (str *ns*) #"\."))))
    :read server/repl-read
    :eval (fn [form] (binding [clojure.test/*test-out* *out*] (eval form)))
    :print clojure.pprint/pprint))

(defn fipp-repl
  []
  (clojure.main/repl
    :prompt (fn [] (printf "%s=> " (peek (str/split (str *ns*) #"\."))))
    :eval (fn [f] (binding [clojure.test/*test-out* *out*] (eval f)))
    :read server/repl-read
    :print fipp/pprint))

(defmacro the-env []
  (into {} (for [k (keys &env)]
             [(name k) k])))

(defmacro nocommit-repl []
  `(clojure.main/repl
    :init   (fn []
              (remove-ns '~'temp)
              (create-ns '~'temp)
              (doseq [[binding# value#] (the-env)]
                (intern '~'temp (symbol binding#) value#)))
    :prompt (fn [] (printf "paused.%s=> " (peek (clojure.string/split (str *ns*) #"\."))))
    :eval (fn [f#] (binding [clojure.test/*test-out* *out*] (eval f#)))
    :read clojure.core.server/repl-read
    :print clojure.pprint/pprint))
