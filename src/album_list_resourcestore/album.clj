(ns album-list-resourcestore.album
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.string]
            [environ.core :refer [env]]
            [cheshire.core :as cheshire :refer :all]
            [album-list-resourcestore.artist :as artist :refer (get-artist-name)]
            [album-list-resourcestore.event :as event :refer [add-event]]))

(def server1-conn {:pool {} :spec {:uri (str "redis://" (env :redis-host) ":6379/")}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn convert-value-to-integer [album-vector key]
  (vector
   (keyword key)
   (Integer/parseInt (get album-vector
                          (inc (.indexOf
                                album-vector
                                key))))))

(defn should-be-integer-value [key]
  (if (= key "year") true false))

(defn create-key-value-vector [key album-vector]
  (cond
    (or (= (name key) "formats")
        (= (name key) "songs"))
         (vector
          (keyword key)
          (cheshire/parse-string
           (get album-vector
                (inc (.indexOf album-vector key)))))
    (should-be-integer-value key) (convert-value-to-integer album-vector key)
    (and (not (= (name key) "formats"))
         (not (= (name key) "songs"))) (vector
                                          (keyword key)
                                          (get album-vector
                                               (inc (.indexOf
                                                     album-vector key))))))

(defn convert-vector-to-hashmap [album-vector]
  (into (hash-map)
        (map #(create-key-value-vector % album-vector)
             (take-nth 2 album-vector))))

(defn get-album [album-id]
  "returns the information of the requested album (in redis gets an album HASH from 'album:id')"
  (convert-vector-to-hashmap (wcar* (car/hgetall (str "album:" album-id)))))

(defn get-artists-names [ids]
  (wcar* (mapv #(car/get (str "artist:" % ":name")) ids)))

(defn get-album-key-value-as-string [album key]
  (if (or (= (name key) "formats")
          (= (name key) "songs"))
    (vector (name key) (cheshire/generate-string (get album key)))
    (vector (name key) (get album key))))

(defn get-album-data-as-list [album]
  (flatten
   (cons (str "album:" (get album :id))
         (mapv #(get-album-key-value-as-string album %) (keys album)))))

(defn add-album [album]
  (let [album-id (get album :id)]
  (wcar* (apply car/hmset
                (get-album-data-as-list album))
         (car/sadd (str "artist:" (get album :artist-id) ":albums") album-id)
         (car/set (str "album:" album-id ":name") (get album :name)))))

(defn post-album [album]
  "adds an album to the database and also the artist if it does not exist yet (in redis adds an album hash with given data as 'album:id'. Then in redis gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' strings and sees if the artist name in the body exists. If it does, then pushes the created albumId to the 'artist:id:albums' SET. Else creates new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name and then pushes the created albumId to the 'artist:id:albums' SET). Return created album's information."
  (add-event "Add album"
             (apply hash-map (flatten
                              (mapv #(get-album-key-value-as-string album %)
                                    (keys album)))))
  (add-album album)
  album)

(defn album-exists? [album-id]
  (= 1 (wcar* (car/exists (str "album:" album-id)))))

(defn put-album [album]
  (add-event "Update album"
             (apply hash-map (flatten
                              (mapv #(get-album-key-value-as-string album %)
                                    (keys album)))))
  (add-album album)
  album)

(defn remove-album [album-id artist-id]
  (wcar* (car/srem (str "artist:" artist-id ":albums") album-id)
         (car/del (str "album:" album-id ":name"))
         (car/del (str "album:" album-id))))

(defn delete-album [album-id]
  (let [album (get-album album-id)]
    (add-event "Delete album"
               (apply hash-map (flatten
                              (mapv #(get-album-key-value-as-string album %)
                                    (keys album)))))
    (remove-album album-id (get album :artist-id))
    album))
