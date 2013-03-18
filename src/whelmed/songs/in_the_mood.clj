(ns whelmed.songs.in-the-mood
  (:use
    [leipzig.melody]
    [whelmed.melody]
    [leipzig.scale] 
    [leipzig.canon] 
    [overtone.inst.sampled-piano]
    [whelmed.contrib.organ-cornet]
    [whelmed.instrument]) 
  (:require
    [overtone.live :as overtone]
    [clojure.math.numeric-tower :as math]
    [overtone.inst.drum :as drums]
    [overtone.synth.stringed :as strings])) 

(defn lilt [time]
  (let [whole-part (math/floor time)
        remainder (- time whole-part)]
    (if (<= remainder 1/2)
      (+ whole-part (* 4/3 remainder))
      (+ whole-part (* 2/3 remainder)))))

(defn swing [notes]
  (map
    (fn [{time :time, duration :duration, :as note}]
      (-> note
        (assoc :time (lilt time))
        (assoc :duration (- (lilt (+ time duration)) (lilt time)))))
    notes))

(defn in-time [speed notes]
  (->> notes
    (where :time speed)
    (where :duration speed)))

(defn tap [drum times length]
  (map #(zipmap [:time :duration :drum] [%1 (- length %1) drum]) times))

(def riff
  (let [melody (->>
          (phrase (cycle [1/2]) (cycle [2 4 7]))
          (take 11)
          (then (phrase [5/2] [7]))
          (with (phrase [7 1] [0 0]))
          (where :part (is ::melody))) 
        extra (->>
          (phrase (repeat 1) [0 1 2 3 4 3 2 1])
          (where :pitch #(- % 7)) 
          (where :part (is ::harmony))) 
        beat (->>
          (tap :tick [1 3 5 7] 8)
          (with (tap :kick [0 1/2 3/2 5/2 7/2 8/2 10/2 11/2 13/2] 8))
          (where :part (is ::beat)))
        chords (->>
          (phrase (cycle [7 1]) [0 0 0 0 3 3 0 0 -3 -3 0 0])
          (where :pitch low)
          (canon (comp (interval 2) (partial canon (interval 2))))
          (canon (interval 7))
          (where :part (is ::chords)))]
  (->>
    (->> melody (times 4) (then (->> melody (where :pitch inc))) (then melody))
    (with chords)
    (with (->> beat (with extra) (times 6)))
    swing
    (in-time (bpm 180))
    (wherever :pitch, :pitch (comp G major)))))

(def kit {:kick drums/kick2
          :tick drums/closed-hat,
          :tock drums/open-hat})

(defmethod play-note ::harmony [note] (pick 0.90 0.3 note))
(defmethod play-note ::melody [{midi :pitch}] (sampled-piano midi))
(defmethod play-note ::beat [note] ((-> note :drum kit)))
(defmethod play-note ::chords [{midi :pitch, length :duration}]
  (organ-cornet (overtone/midi->hz midi) length 0.1))

(comment
  (def riff nil) 
  (jam riff)
  (jam beated)
) 
