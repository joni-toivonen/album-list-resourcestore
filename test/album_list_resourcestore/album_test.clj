(ns album-list-resourcestore.album-test
  (:require [midje.sweet :refer :all]
            [album-list-resourcestore.album :refer :all]
            [album-list-resourcestore.artist :as artist :refer (get-artist-name)]))

(facts "Album handler tests"
       (fact "get-album-data-as-list should take album schema and album id and transfer them into a list"
             (with-redefs [get-artist-name (fn [_] "test-artist")]
               (let [album-id 123
                     album {:name "test-album"}]
                 (get-album-data-as-list album album-id) => '("album:123" "id" 123 "artist" "test-artist" "name" "test-album"))))
       (fact "get-album-key-value-as-string should return a vector containing given key and value"
             (let [album {:id 123 :name "test-album"}]
               (get-album-key-value-as-string album :name) => ["name" "test-album"]))
       (fact "get-album-key-value-as-string should return a vector containing key and value vectors if key is :formats"
             (let [album {:id 123 :formats ["cd" "lp"]}]
               (get-album-key-value-as-string album :formats) => [["formats0" "cd"] ["formats1" "lp"]]))
       (fact "get-album-key-value-as-string should return a vector containing key and value vectors if key is :songs"
             (let [album {:id 123 :songs ["test-song1" "test-song2"]}]
               (get-album-key-value-as-string album :songs) => [["songs0" "test-song1"] ["songs1" "test-song2"]]))
       (fact "handle-multiple-items should convert songs in album vector to a hash-map with key :songs and value as vector of songs"
             (let [album-vector ["name" "test-album" "songs0" "test-song1" "songs1" "test-song2"]
                   index-of-first-item (.indexOf (into [] (take-nth 2 album-vector)) "songs0")
                   vector-string "songs"]
               (handle-multiple-items index-of-first-item album-vector vector-string) => [:songs ["test-song1" "test-song2"]]))
       (fact "convert-value-to-integer should convert numerical string value to integer"
             (let [album-vector ["id" "2"]
                   key "id"]
               (convert-value-to-integer album-vector key) => [:id 2]))
       (fact "should-be-integer-value returns true if key is either id, artist-id or year. Else it should return false."
             (map #(should-be-integer-value %) ["id" "artist-id" "year" "something"]) => '(true true true false))
       (fact "convert-vector-to-hashmap should convert vector of album data into hash-map"
             (let [album-vector ["id" "1" "artist" "Abba" "name" "testalbum" "artist-id" "1" "formats0" "cd" "label" "testlabel" "year" "2000" "songs0" "song1" "songs1" "song2" "songs2" "song3"]]
               (convert-vector-to-hashmap album-vector) => {:id 1, :artist "Abba", :name "testalbum", :artist-id 1, :formats ["cd"], :label "testlabel", :year 2000, :songs ["song1" "song2" "song3"]})))
