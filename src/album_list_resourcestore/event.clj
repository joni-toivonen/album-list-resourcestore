(ns album-list-resourcestore.event
  (:require [taoensso.carmine :as car :refer (wcar)]
            [environ.core :refer [env]]))

(def server1-conn {:pool {} :spec {:uri (str "redis://" (env :redis-host) ":6379/")}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn generate-event-id []
  (wcar* (car/incr "event:id")))

(defn get-latest-event-id []
  (let [event-id (wcar* (car/get "event:id"))]
    (if event-id (Integer/parseInt event-id) event-id)))

(defn get-event [event-id]
  (wcar* (car/parse-map (car/hgetall (str "event:" event-id)))))

(defn get-events
  ([]
   (let [latest-event-id (get-latest-event-id)]
     (if latest-event-id
       (map #(get-event %) (range 1 (inc latest-event-id)))
       [])))
  ([event-id]
   (let [latest-event-id (get-latest-event-id)]
     (if latest-event-id
       (map #(get-event %) (range (inc event-id) (inc latest-event-id)))
       []))))

(defn add-event [event-type event-data]
  (let [event-id (generate-event-id)]
    (wcar* (car/hmset*
            (str "event:" event-id)
            (conj {:event-id event-id}
                  {:event-type event-type}
                  event-data)))))
