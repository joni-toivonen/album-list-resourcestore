(ns album-list-resourcestore.discogs
  (:require [clj-http.client :as client]
            [environ.core :refer [env]]
            [cheshire.core :as cheshire :refer :all]))

(defn get-resource [url]
  (let [search-result (cheshire/parse-string (get (client/get url) :body))]
    {:name (get search-result "title")
     :label (get (first (get search-result "labels")) "name")
     :year (get search-result "year")
     :songs (map #(get % "title") (get search-result "tracklist"))}))

(defn get-album-data [data]
  (let [result (first (get data "results"))]
    (if (contains? result "resource_url")
      (get-resource (get result "resource_url"))
      [])))

(defn search [barcode]
  (let [auth-token (env :discogs-token)
        search-result (client/get "https://api.discogs.com/database/search"
                                  {:query-params {"q" barcode}
                                   :headers {"Authorization" (str "Discogs token=" auth-token)}})]
    (cond
      (= (get search-result :status) 200) (get-album-data (cheshire/parse-string
                                                           (get search-result :body)))
      :else [])))
