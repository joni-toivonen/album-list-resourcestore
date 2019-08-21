(ns album-list-resourcestore.artist-test
  (:require [midje.sweet :refer :all]
            [album-list-resourcestore.artist :refer :all]))


(facts "Artist handler tests"
       (fact "get-artist-ids should return a vector of artist ids"
             (with-redefs [get-artist-ids (fn [] ["1234" "5678"])
                           get-artist-name (let [results (atom ["artist1" "artist2"])]
                                             (fn [_]
                                               (let [result (first @results)]
                                                 (swap! results rest)
                                                 result)))]
               (get-artists) => '({:id "1234", :name "artist1"}
                                  {:id "5678", :name "artist2"})))
       (fact "get-albums-from-artist should return a vector of hash-maps containing album ids and names"
             (with-redefs [get-album-ids (fn [_] ["1234"])
                           get-album-name (fn [_] "album1")]
               (get-albums-from-artist 123) => '({:name "album1", :id "1234"}))))
