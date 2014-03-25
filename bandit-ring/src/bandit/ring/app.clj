(ns bandit.ring.app
  (:use [compojure.core]
        [ring.middleware stacktrace reload]
        [ring.util.response]
        [ring.adapter.jetty :only (run-jetty)])
  (:require [bandit.ring.adverts :as ads]
            [bandit.ring.rank :as rank]
            [hiccup.page :as page]))

(defn layout
  [title & body]
  (page/html5
   [:head [:title title]]
   [:body
    [:h1 title]
    body]))

(defroutes main-routes
  (GET "/" []
       (layout "Bandit Examples"
               [:div#main
                [:ul
                 [:li
                  [:a {:href "/ads"} "Adverts example"]]
                 [:li
                  [:a {:href "/rank"} "Ranking items example"]]]]))
  (GET "/ads" []
       (layout "Advertisement Click-through"
               [:div#main
                (ads/advert-html)]))
  (GET "/ads/click/:arm-name" [arm-name]
       (dosync (alter ads/bandit ads/record-click (keyword arm-name)))
       (redirect "/ads"))
  (GET "/rank" []
       (layout "Ranking items"
               [:div#main (rank/items-html)]))
  (GET "/rank/click/:arm-name" [arm-name]
       (dosync (alter rank/bandit rank/record-click (keyword arm-name)))
       (redirect "/rank")))

(def app (-> main-routes
             (wrap-reload '(bandit.ring app adverts))
             (wrap-stacktrace)))

(defn -main
  []
  (run-jetty #'app {:port 8080}))
