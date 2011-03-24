(ns test-async-fetch.bench
  (:use lamina.core
        [clojure.contrib.io :only [read-lines]])
  (:require [clj-http.client :as c]
            [http.async.client :as a]))

(defn get-urls [file] (read-lines file))

(defn fetch-with-apache
  [url]
  (try
    (->
     (c/get url)
     (:status))
    (catch Exception e
      400)))

(defn test-lamina
  [urls]
   (let [chan (apply channel urls)]
     (receive-all chan fetch-with-apache)))

(defn test-sync
  [urls]
   (for [url urls]
     (fetch-with-apache url)))

(defn test-pmap
  [urls]
   (pmap fetch-with-apache urls))

(defn test-async
  [urls]
   (a/with-client {:follow-redirects true}
     (let [arr (map #(a/GET %) urls)]
       (for [res arr]
         (do
           (a/await res)
           (:code (a/status res)))))))

(defn -main
  [file]
  (test-pmap (get-urls file)))
