(ns album-list-resourcestore.artist-test
  (:require [cheshire.core :as cheshire]
            [midje.sweet :refer :all]
            [album-list-resourcestore.artist :refer :all]))

(defn parse-body [body]
  (cheshire/parse-string (slurp body) true))

(facts "Artist handler tests"

       (fact "get-artists should return a list of artists"
             (get-artists) => '({:id 1, :name "artist1"} {:id 2, :name "artist2"})))
