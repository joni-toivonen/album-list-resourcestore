(ns album-list-resourcestore.album
  (:require [album-list-resourcestore.artist :refer [create-artist add-album-to-artist]]
            [taoensso.carmine :as car :refer (wcar)]))

(def server1-conn {:pool {} :spec {:uri "redis://127.0.0.1:6379/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn generate-album-id []
  (wcar* (car/incr "album:id")))

(defn get-album [album-id]
  "returns the information of the requested album (in redis gets an album HASH from 'album:id')"
  (wcar* (car/hgetall (str "album:" album-id))))

(defn get-artists-names [ids]
  (wcar* (mapv #(car/get (str "artist:" % ":name")) ids)))

(defn create-key-values-from-vector [key album-vector]
  (into [] (map #(vector (str (name key) (.indexOf album-vector %)) %) album-vector)))

(defn get-album-key-value-as-string [album key]
  (if (or (= (name key) "format")
          (= (name key) "songs"))
    (create-key-values-from-vector key (get album key))
    (vector (name key) (get album key))))

(defn get-album-data-as-list [album album-id]
  (flatten
   (cons (str "album:" album-id)
         (into [] (map #(get-album-key-value-as-string album %) (keys album))))))

(defn add-album [album album-id]
  (wcar* (apply car/hmset
                (get-album-data-as-list album album-id))
         (car/sadd (str "artist:" (get album :artist-id) ":albums") album-id)
         (car/set (str "album:" album-id ":name") (get album :name))))

(defn post-album [album]
  "adds an album to the database and also the artist if it does not exist yet (in redis adds an album hash with given data as 'album:id'. Then in redis gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' strings and sees if the artist name in the body exists. If it does, then pushes the created albumId to the 'artist:id:albums' SET. Else creates new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name and then pushes the created albumId to the 'artist:id:albums' SET). Return created album's information."
  (let [album-id (generate-album-id)]
    (add-album album album-id)
    (get-album album-id)))
