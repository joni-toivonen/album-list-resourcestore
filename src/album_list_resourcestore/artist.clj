(ns album-list-resourcestore.artist
  (:require [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {} :spec {:uri "redis://127.0.0.1:6379/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn get-artist-ids []
  (wcar* (car/smembers "artists")))

(defn get-artist-name [id]
  (wcar* (car/get (str "artist:" id ":name"))))

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

(defn get-album-name [id]
  (wcar* (car/get (str "album:" id ":name"))))

(defn get-album-ids [artist-id]
  (wcar* (car/smembers (str "artist:" artist-id ":albums"))))

(defn get-albums-from-artist [artist-id]
  "returns albums that the artist has made as array of albums with id and name (in redis gets an album SET from 'artist:id:albums' and names of those albums from 'album:id:name' STRINGs)"
  (let [album-ids (get-album-ids artist-id)]
    (map #(hash-map :id %, :name (get-album-name %))
         album-ids)))

(defn create-artist [artist-name]
  "creates new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name. Returns the created artist's id")

(defn add-album-to-artist [artist-id album-id]
  "adds albumId to the 'artist:id:albums' SET"
  (car/sadd (str "artist:" artist-id ":albums") album-id))
