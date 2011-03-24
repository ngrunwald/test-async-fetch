(ns test-async-fetch.bench
  (:use lamina.core
        [clojure.contrib.io :only [read-lines]]
        clojure.contrib.profile)
  (:require [clj-http.client :as c]
            [http.async.client :as a]))

(defn get-urls []  (read-lines "urls.txt"))

(defn fetch-with-apache
  [url]
  (->
   (c/get url)
   (:status)))

(defn test-lamina
  [urls]
  (prof :lamina
        (let [chan (apply channel urls)]
          (receive-all chan fetch-with-apache))))

(defn test-sync
  [urls]
  (prof :sync
        (for [url urls]
          (fetch-with-apache url))))

(defn test-pmap
  [urls]
  (prof :pmap
        (pmap fetch-with-apache urls)))

(defn test-async
  [urls]
  (prof :async
        (a/with-client {:follow-redirects true}
          (let [arr (map #(a/GET %) urls)]
            (for [res arr]
              (do
                (a/await res)
                (:code (a/status res))))))))
