(ns test-async-fetch.bench
  (:use lamina.core
        [clojure.contrib.io :only [read-lines]])
  (:require [clj-http.client :as c]))

(def urls (read-lines "urls.txt"))

(defn test-lamina
  [urls]
  (time
   (let [chan (apply channel urls)]
     (receive-all chan (fn [url]
                         (->
                          (c/get url)
                          (:status)))))))



