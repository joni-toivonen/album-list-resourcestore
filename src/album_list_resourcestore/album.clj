(ns album-list-resourcestore.album
  (:require [taoensso.carmine :as car :refer (wcar)]
            [clojure.string]
            [album-list-resourcestore.artist :as artist :refer (get-artist-name)]))

(def server1-conn {:pool {} :spec {:uri "redis://127.0.0.1:6379/"}})
(defmacro wcar* [& body] `(car/wcar server1-conn ~@body))

(defn generate-album-id []
  (wcar* (car/incr "album:id")))

(defn handle-multiple-items [index-of-first-item album-vector vector-string]
  (let [album-keys (take-nth 2 album-vector)
        album-values (take-nth 2 (rest album-vector))
        index-of-last-item (.lastIndexOf
                            (mapv #(clojure.string/includes? % vector-string)
                                  album-keys)
                            true)
        items (mapv #(nth album-values %) (range index-of-first-item
                                                 (inc index-of-last-item)))]
    [(keyword vector-string) items]))

(defn convert-value-to-integer [album-vector key]
  (vector
   (keyword key)
   (Integer/parseInt (get album-vector
                          (inc (.indexOf
                                album-vector
                                key))))))

(defn should-be-integer-value [key]
  (if (or (= key "id")
          (= key "artist-id")
          (= key "year")) true false))

(defn create-key-value-vector [key album-vector]
  (cond
    (and (clojure.string/includes? key "songs")
         (= key "songs0")) (handle-multiple-items
                           (.indexOf (into [] (take-nth 2 album-vector)) key)
                           album-vector "songs")
    (and (clojure.string/includes? key "formats")
         (= key "formats0")) (handle-multiple-items
                             (.indexOf (into [] (take-nth 2 album-vector)) key)
                             album-vector "formats")
    (and (should-be-integer-value key)) (convert-value-to-integer album-vector key)
    (and (not (clojure.string/includes? key "songs"))
         (not (clojure.string/includes? key "formats"))) (vector
                                                         (keyword key)
                                                         (get album-vector
                                                              (inc (.indexOf
                                                                    album-vector
                                                                    key))))))

(defn convert-vector-to-hashmap [album-vector]
  (into (hash-map)
        (map #(create-key-value-vector % album-vector)
             (take-nth 2 album-vector))))

(defn get-album [album-id]
  "returns the information of the requested album (in redis gets an album HASH from 'album:id')"
  (convert-vector-to-hashmap (wcar* (car/hgetall (str "album:" album-id)))))

(defn get-artists-names [ids]
  (wcar* (mapv #(car/get (str "artist:" % ":name")) ids)))

(defn create-key-values-from-vector [key album-vector]
  (into [] (map #(vector (str (name key) (.indexOf album-vector %)) %) album-vector)))

(defn get-album-key-value-as-string [album key]
  (if (or (= (name key) "formats")
          (= (name key) "songs"))
    (create-key-values-from-vector key (get album key))
    (vector (name key) (get album key))))

(defn get-album-data-as-list [album album-id]
  (flatten
   (cons (str "album:" album-id)
         (cons
          ["id" album-id "artist" (get-artist-name (get album :artist-id))]
          (into [] (map #(get-album-key-value-as-string album %) (keys album)))))))

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
