(ns album-list-resourcestore.artist
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {} :spec {:uri "redis://127.0.0.1:6379/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn get-artist-ids []
  (wcar* (car/smembers "artists")))

(defn get-artist-name [id]
  (str "artist" id))

(defn get-artists-names-and-ids [ids]
  "Takes a list of artist ids as parameter and gets the names of the artists and returns a hash-map containing each artist's id and name"
  (map #(hash-map :id %, :name (get-artist-name %))
       ids))

(defn get-artists []
  "returns a list of all artists as array of artists with id and name
(in redis first gets an artists SET 'artists' which consists of artist IDs,
then gets the names of those artists from 'artist:id:name' STRINGs)"
  (let [ids (get-artist-ids)]
    (get-artists-names-and-ids ids)))
