(ns whelmed.songs.at-all
  (:use
    leipzig.scale
    leipzig.melody
    leipzig.live
    leipzig.chord
    [overtone.live :only [midi->hz]]
    whelmed.melody
    whelmed.instrument))

(defn with-bass [chord]
  (-> chord (assoc :bass (lower (:i chord)))))

(def I (-> triad (root 0) with-bass))
(def II (-> triad (root 1) with-bass))
(def V (-> triad (root 4) with-bass))

(def progression [I I II II II V I (update-in V [:bass] lower)])

(def rhythm-n-bass
  (let [bass (fn [chord]
               (phrase [3 1] (repeat (:bass chord))))
        rhythm (fn [chord]
                 (->> [(dissoc chord :bass)]
                   (phrase [2])
                   (after 2)))
        once #(with (rhythm %) (bass %))]
    (->> progression
         (mapthen once)
         (all :part ::default))))

(def intro
  (->> (phrase (repeat 32 1) (cycle [5 4]))
    (all :part ::default)
    (with rhythm-n-bass)))

(def melody
  (letfn [(rhyth [n] (concat (repeat n 1/2) [(- 9 (/ n 2))]))]
    (->>
      (after -1 (phrase (rhyth 7) [2 4 5 4 4 2 4 nil]))
      (then
        (after -1 (phrase (rhyth 7) [-2 1 2 1 1 -2 1 nil])))
      (then
        (after -1 (phrase (rhyth 10) [-2 1 2 1 1 -2 1 2 3 4 nil])))
      (then
        (after -1 (phrase (rhyth 10) [-1 -2 -3 0 0 -3 0 1 0 -3 nil])))
      (all :part ::dux)
      (with rhythm-n-bass))))

(def answer
  (->>
    (after 9/2 (phrase (repeat 1/2) [11 11 12 9 7]))
    (then
      (after 11/2 (phrase (repeat 1/2) [8 8 9 8 3])))
    (then
      (after 11/2 (phrase (repeat 1/2) [8 8 9 6 4])))
    (then
      (after 11/2 (phrase (repeat 1/2) [11 11 12 11 8])))
    (all :part ::comes)))

(def chorus
  (let [doesnt (phrase (concat (repeat 4 1/2) [6]) [-3 0 0 -3 0])
        doesnt-at-all (->> doesnt
                           (times 3)
                           (then
                             (->> doesnt drop-last
                                  (then (phrase [1/2 1/2 1/2 9/2] [0 1 0 -1]))))
                           (all :part ::dux)
                           (with rhythm-n-bass))
        bada (->> (phrase [1/2 1/2 3/2 2/2 9/2] [0 4 5 4 0])
                  (times 3)
                  drop-last
                  (then (phrase [5/2 1/2 1/2 10/2] [0 1 0 -1]))
                  (after 9/2)
                  (where :pitch raise)
                  (all :part ::comes))]
    (->>
      doesnt-at-all 
      (then (with doesnt-at-all bada)))))

(def finale
  (->> (phrase [1/2 1/2 1/2] [11 13 14])
    (then (after -1/2 (phrase [13/2] [(update-in I [:i] raise)])))
    (all :part ::default)))

(def at-all
  (->>
    intro
    (then melody)
    (then (->> melody (with answer)))
    (then chorus)
    (then (->> melody (with answer)))
    (then (after 3/2 (->> melody (with answer)
                          (then chorus)
                          (in-time (comp (scale [2/3 1/3]) #(* 2 %)))
                          (in-time #(* % 7/8)))))
    (then finale)
    (wherever (comp not :part), :part (is ::default))
    (in-time (bpm 160))
    (where :pitch (comp low D major))))

(defmethod play-note ::comes [{midi :pitch s :duration}]
  (some-> midi midi->hz (corgan s 1 :pan -1/3 :vibrato 32/3 :depth 0.1 :wet 0.6))) 

(defmethod play-note ::default [{midi :pitch}]
  (some-> midi midi->hz (organ 1/8 2 :pan 0 :wet 0.7))) 

(defmethod play-note ::dux [{midi :pitch s :duration}]
  (some-> midi midi->hz (corgan s 2 :pan 1/3 :vibrato 4/3 :depth 0.5 :wet 0.6 :limit 1500))) 

(comment
  (play at-all)
  (jam (var at-all))
) 
