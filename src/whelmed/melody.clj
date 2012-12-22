(ns whelmed.melody
  (:use
    [leipzig.melody]
    [leipzig.chord]
    [whelmed.instrument]
    [overtone.live :only [midi->hz]]
    [leipzig.scale]))

(defn from [base] (partial + base))

(defn but [from to f notes]
  (let [early? #(< (:time %) from)
        late? #(>= (:time %) to)
        apple (->> notes
                (filter #(or (early? %) (late? %)))) 
        core (->> notes
               (filter #(not (early? %))) 
               (filter #(not (late? %))))] 
    (with apple (f core))))

(defn demo
  ([notes] (demo major notes))
  ([scale notes]
    (->> notes
      (where :time (bpm 90))
      (where :duration (bpm 90))
      (where :pitch (comp C scale))
      play)))

(defn cluster [duration pitches]
  (map
    #(zipmap
      [:time :duration :pitch]
      [0 duration %])
    pitches))

(defn raise [chord k n] (update-in chord [k] (from n)))

(defn inversion [chord n]                                                                      
  (cond
    (= n 1)
      (-> chord (root -7) (raise :i 7))
    (= n 2)
      (-> chord (inversion 1) (raise :iii 7))))


(defmethod play-note :default [{midi :pitch}]  (-> midi midi->hz  (bell 5000)))

(def bassline
  (phrase [1 2/3 1/3] [1 2 4])
)

(def it
  (->> bassline
    (where :pitch (comp G flat blues))
    (where :time (bpm 100))
    (where :duration (bpm 100))))

(defn forever [fragment] (->> @fragment (then (lazy-seq (forever fragment)))))
(defn jam [it] (->> it forever play)) 

;(jam (var it))
