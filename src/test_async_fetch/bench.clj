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
  (doall
   (pmap fetch-with-apache urls)))

(defn test-async
  [urls]
  (with-open [client (a/create-client :follow-redirects true)]
    (let [arr (doall (map a/GET urls))]
      (doall (for [res arr]
               (:code (a/status (a/await res))))))))

(defn -main
  [file]
  (let [urls (get-urls file)]
    (println "apache")
    (time
     (println (test-pmap urls)))
    (println "async")
    (time
     (println (test-async urls)))))
