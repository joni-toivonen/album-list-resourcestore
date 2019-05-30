(ns album-list-resourcestore.album
  (:require [album-list-resourcestore.artist :refer [create-artist add-album-to-artist]]
            [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {} :spec {:uri "redis://127.0.0.1:6379/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn get-album [album-id]
  "returns the information of the requested album (in redis gets an album HASH from 'album:id')"
  (wcar* (car/hgetall (str "albums:" album-id))))

(defn get-artists-names [ids]
  (wcar* (mapv #(car/get (str "artist:" % ":name")) ids)))

(defn add-album [Album]
  (wcar* (apply car/hset
                (cons (str "album:" (get Album :id))
                      (into [] cat (map #(str (name %) (get Album %)) (keys Album)))))
         (car/sadd (str "artist:" (get Album :artist-id) ":albums") (get Album :id))))

(defn post-album [Album]
  "adds an album to the database and also the artist if it does not exist yet (in redis adds an album hash with given data as 'album:id'. Then in redis gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' strings and sees if the artist name in the body exists. If it does, then pushes the created albumId to the 'artist:id:albums' SET. Else creates new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name and then pushes the created albumId to the 'artist:id:albums' SET)"
  (if (nil? (get Album :artist-id))
    (add-album (assoc Album :artist-id (create-artist (get Album :artist))))
    (add-album Album)))
