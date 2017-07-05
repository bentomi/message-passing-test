(ns chain.agents
  (:import [java.util.concurrent SynchronousQueue Executors]))

(defn relay [s m]
  (if (instance? clojure.lang.Agent s)
    (send s relay m)
    (.put ^SynchronousQueue s m))
  s)

(defn create-senders [m start]
  (reduce (fn [next _] (agent next)) start (range (dec m))))

(defn run [m n]
  (let [message (into () (range 20))
        q (SynchronousQueue.)
        start (create-senders m (agent q))]
    (dotimes [_ n]
      (send start relay message))
    (dotimes [_ n]
      (.take q))))

(defn -main [#_p m n]
  (set-agent-send-executor! #_(Executors/newFixedThreadPool (Integer/parseInt p))
                            (Executors/newWorkStealingPool))
  (time (run (Integer/parseInt m) (Integer/parseInt n)))
  (shutdown-agents))
