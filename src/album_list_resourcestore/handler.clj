(ns album-list-resourcestore.handler
  (:require [album-list-resourcestore.artist :as artist :refer [get-artists get-albums-from-artist post-artist]]
            [album-list-resourcestore.album :as album :refer [get-album post-album put-album delete-album album-exists?]]
            [album-list-resourcestore.event :as event :refer [get-events]]
            [album-list-resourcestore.discogs :as discogs :refer [search]]
            [clojure.tools.logging :as log :refer :all]
            [compojure.api.sweet :refer :all]
            [ring.util.http-response :refer :all]
            [schema.core :as s]))

(s/defschema Album
  "A schema for album"
  {:id s/Uuid
   :name s/Str
   :artist s/Str
   :artist-id s/Uuid
   :formats [s/Str]
   (s/optional-key :label) s/Str
   (s/optional-key :year) s/Int
   (s/optional-key :extra) s/Str
   (s/optional-key :songs) [s/Str]})

(s/defschema Artist
  "A schema for artist"
  {:id s/Uuid
   :name s/Str})

(def app
  (api
    {:swagger
     {:ui "/"
      :spec "/swagger.json"
      :data {:info {:title "Album-list-resourcestore"
                    :description "Compojure Api example"}
             :tags [{:name "api", :description "some apis"}]}}}

    (context "/api" []
      :tags ["api"]

      (GET "/events" []
           (ok (get-events)))

      (GET "/events/:eventId" []
           :path-params [eventId :- s/Int]
           (s/validate (s/pred pos?) eventId)
           (ok (get-events eventId)))

      (GET "/artists" []
           :summary "returns a list of all artists as array of artists with id and name (in redis first gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' STRINGs)"
           (ok (get-artists)))

      (POST "/artists" []
            :return Artist
            :body [artist Artist]
            :summary "creates new artistId using car/incr 'artist:id' and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name'. Returns the created artist's id"
            (ok (post-artist artist)))

      (GET "/artists/:artistId" []
           :path-params [artistId :- s/Uuid]
           :summary "returns albums that the artist has made as array of albums with id and name (in redis gets an album SET from 'artist:id:albums' and names of those albums from 'album:id:name' STRINGs)"
           (ok (get-albums-from-artist artistId)))

      (PUT "/artists/:artistId" []
           :path-params [artistId :- s/Uuid])

      (GET "/albums/:albumId" []
           :responses {200 {:schema Album}
                       404 {}}
           :path-params [albumId :- s/Uuid]
           :summary "returns the information of the requested album (in redis gets an album HASH from 'album:id')"
           (let [album (get-album albumId)]
             (if (empty? album)
               (not-found (str "Album with id " albumId " not found"))
               (ok (get-album albumId)))))

      (PUT "/albums/:albumId" []
           :responses {200 {:schema Album}
                       404 {}}
           :path-params [albumId :- s/Uuid]
           :body [album Album]
           :summary "updates an album in the database and also adds an artist if it does not exist yet (in redis gets the album HASH from 'album:id' and compares if the artist is updated. If it is, then removes the album id from 'artist:id:albums' and adds it to the correct artist's SET if it exists. If the updated artist name does not exist in 'artist:id:name' then creates a new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name'. Also if the album name has changed, updates the old name STRING 'album:id:name'. After that it updates the HASH with given data.)"
           (log/info "PUT album" album)
           (if (album-exists? albumId)
             (ok (put-album album))
             (not-found (str "No such album with id " albumId))))

      (POST "/albums" []
            :return Album
            :body [album Album]
            :summary "adds an album to the database and also the artist if it does not exist yet (in redis adds an album hash with given data as 'album:id'. Then in redis gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' strings and sees if the artist name in the body exists. If it does, then pushes the created albumId to the 'artist:id:albums' SET. Else creates new artistId and pushes it to the 'artists' SET and also adds a new string with the artist name to 'artist:id:name and then pushes the created albumId to the 'artist:id:albums' SET)"
            (log/info "POST album" album)
            (ok (post-album album)))

      (DELETE "/albums/:albumId" []
              :return Album
              :path-params [albumId :- s/Uuid]
              :summary "removes an album from the database (in redis removes the album from 'artist:id:albums' SET, album:id:name key and the 'album:id' album hash)"
              (log/info "DELETE album" albumId (album-exists? albumId))
              (if (album-exists? albumId)
                (ok (delete-album albumId))
                (not-found (str "No such album with id " albumId))))

      (GET "/search" [barcode]
           :summary "returns a list of all artists as array of artists with id and name (in redis first gets an artists SET 'artists' which consists of artist IDs, then gets the names of those artists from 'artist:id:name' STRINGs)"
           (ok (search barcode))))))
