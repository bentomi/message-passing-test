(ns chain.async
  (:require [clojure.core.async :as async :refer [<! >! <!!]]))

(comment
  (defonce my-executor
    (let [executor-svc (java.util.concurrent.Executors/newWorkStealingPool)]
      (reify clojure.core.async.impl.protocols/Executor
        (clojure.core.async.impl.protocols/exec [this r]
          (.execute executor-svc ^Runnable r)))))

  (alter-var-root #'clojure.core.async.impl.dispatch/executor
                  (constantly (delay my-executor))))

(defn relay [in out]
  (async/go-loop []
    (>! out (<! in))
    (recur)))

(defn create-senders [m start]
  (reduce (fn [in _] (let [out (async/chan 1000)] (relay in out) out))
          start (range m)))

(defn run [m n]
  (let [message (into () (range 20))
        start (async/chan 1000)
        end (create-senders m start)]
    (async/go
      (dotimes [_ n]
        (>! start message)))
    (dotimes [_ n]
      (<!! end))))

(defn -main [m n]
  (time (run (Integer/parseInt m) (Integer/parseInt n))))
