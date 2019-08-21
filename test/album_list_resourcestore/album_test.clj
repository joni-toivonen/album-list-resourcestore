(ns album-list-resourcestore.album-test
  (:require [midje.sweet :refer :all]
            [album-list-resourcestore.album :refer :all]))

(facts "Album handler tests"
       (fact "get-album-data-as-list should take album schema and album id and transfer them into a list"
             (let [album-id 123
                   album {:id album-id :name "test-album"}]
               (get-album-data-as-list album album-id) => '("album:123" "id" 123 "name" "test-album")))
       (fact "get-album-key-value-as-string should return a vector containing given key and value"
             (let [album {:id 123 :name "test-album"}]
               (get-album-key-value-as-string album :name) => ["name" "test-album"]))
       (fact "get-album-key-value-as-string should return a vector containing key and value vectors if key is :format"
             (let [album {:id 123 :format ["cd" "lp"]}]
               (get-album-key-value-as-string album :format) => [["format0" "cd"] ["format1" "lp"]]))
       (fact "get-album-key-value-as-string should return a vector containing key and value vectors if key is :songs"
             (let [album {:id 123 :songs ["test-song1" "test-song2"]}]
               (get-album-key-value-as-string album :songs) => [["songs0" "test-song1"] ["songs1" "test-song2"]])))
