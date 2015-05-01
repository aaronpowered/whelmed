(ns whelmed.songs.my-friend
  (:require [overtone.live :refer :all]
            [leipzig.melody :refer :all]
            [leipzig.scale :as scale]
            [leipzig.canon :as canon]
            [leipzig.live :as live]
            [leipzig.chord :as chord]
            [leipzig.temperament :as temperament]))

; Instruments
(def the-key (comp temperament/equal scale/F scale/major))

(definst bass [freq 110 dur 1.0 volume 1.0 pan 0 wet 0.5 room 0.5]
  (-> (sin-osc freq) 
      (+ (* 1/3 (sin-osc (* 2 freq))))
      (+ (* 1/2 (sin-osc (* 3 freq))))
      (+ (* 1/3 (sin-osc (* 5 freq))))
      (clip2 0.8)
      (rlpf (the-key 14) 1/7)
      (* (env-gen (adsr 0.02 0.2 0.1 0.1) (line:kr 1 0 dur) :action FREE))
      (* volume)
      (pan2 pan)
      (free-verb :mix wet :room room)))

(definst organ [freq 440 dur 1 volume 0.6 pan 0 wet 0.5 room 0.5]
  (-> (square freq)
      (+ (sin-osc 9) (sin-osc (* 2 freq)))
      (+ (sin-osc 9) (sin-osc (* 1.999 freq)))
      (+ (sin-osc 6) (sin-osc (* 4.01 freq)))
      (+ (sin-osc 3) (sin-osc (* 6 freq)))
      (+ (sin-osc 3) (sin-osc (* 1/2 freq)))
      (lpf 4000)
      (* (env-gen (adsr 0.05 0.2 0.7 0.1) (line:kr 1 0 dur) :action FREE))
      (* 1/10 volume)
      (pan2 pan)
      (free-verb :mix wet :room room)
      
      ))

(definst sing [freq 440 dur 1.0 volume 1.0 pan 0 wet 0.5 room 0.5]
  (-> (saw freq)
      (+ (saw (* freq 1.01)))
      (rlpf (mul-add (sin-osc 8) 200 1500) 1/8)
      (* (env-gen (asr 0.03 0.3 0.1) (line:kr 1 0 dur) :action FREE))
      (* 1/4 volume)
      (pan2 pan)
      (free-verb :mix wet :room room)))

(definst kick [freq 220 volume 1.0]
  (-> (line:kr freq (* freq 1/2) 0.5)
      sin-osc 
      (+ (sin-osc freq))
      (+ (sin-osc (/ freq 2) (sin-osc 1)))
      (* (env-gen (perc 0.01 0.1) :action FREE))
      (* volume)))

(definst tip [freq 110 volume 1.0]
  (-> (white-noise)
      (rlpf (* 3 freq) 1/2)
      (* (env-gen (perc 0.01 0.05) :action FREE))
      (* 1/5 volume)))

; Arrangement
(defmethod live/play-note :bass
  [{hertz :pitch seconds :duration}]
  (some-> hertz (bass seconds :pan -1/3 :wet 0.7 :room 0.1)))

(defmethod live/play-note :accompaniment
  [{hertz :pitch seconds :duration}]
  (some-> hertz (organ seconds :wet 0.7 :pan 1/3)))

(defmethod live/play-note :melody
  [{hertz :pitch seconds :duration}]
  (some-> hertz (sing seconds :wet 0.3)))

(defmethod live/play-note :beat
  [{hertz :pitch drum :drum}]
  (some-> hertz drum))

; Composition
(defn power-up [chord]
  (-> chord (chord/inversion 2) (assoc :-i (-> chord :i scale/lower))))

(def chords
  [(-> chord/triad power-up)
   (-> chord/triad (chord/root 1) power-up)
   (-> chord/triad (chord/root 2) power-up)
   (-> chord/triad (chord/root 1) power-up)])

(def bassline
  (let [blat (phrase [2/2 1/2 5/2] [0 0 nil])
        walk (phrase [3/2 2/2 2/2 1/2] [1 -2 1 -2])]
    (->>
      blat
      (then walk)
      (then (->> blat (where :pitch (scale/from 2))))
      (then walk)
      (then blat)
      (then walk)
      (then (->> walk (where :pitch inc)))
      (then walk)
      (filter :pitch)
      (where :pitch (comp scale/lower scale/lower))
      (all :part :bass))))

(def flatline
  (->>
    (phrase (cycle [4 1/2 1/2 3]) (repeat 0))
    (take-while #(-> % :time (< 32)))
    (where :pitch (comp scale/lower scale/lower))
    (all :part :bass)))

(def flock
  (->>
    (phrase (repeat 2) (mapcat repeat (repeat 2) chords))
    (times 2)
    (all :part :accompaniment)))

(def accompaniment
  (->>
    (phrase [3/3 1/3 8/3] [(chords 0) (chords 0) nil])
    (then (phrase [4] [(chords 1)]))
    (then (phrase [3/3 1/3 8/3] [(chords 2) (chords 2) nil]))
    (then (phrase [4] [(chords 1)]))
    (filter :pitch)
    (all :part :accompaniment)
    (times 2)))

(def core-med
  (->>
    (phrase (repeat 1/3) [-7 -5 -3])
    (canon/canon (canon/interval -7))
    (after -1)
    (then (phrase [15 1 7 1 7 1] [7 8 9 8 7 8]))
    (where :pitch scale/raise)
    (all :part :melody)))

(def my-friend
  (let [i-know
        (after 2
               (phrase [2/3 1/3 3/3 5/3 7/3]
                       [2 1 0 1 -2]))
        thats-no-excuse
        (after 2
               (phrase [2/3 1/3 3/3 2/3 1/3 2/3 1/3 2/3 1/3 2/3 7/3]
                [2 1 0 1 0 1 2 1 0 -2 -3]))
        anyone-else
        (phrase [2/3 1/3 3/3 2 2 2 2 4]
                [2 1 0 1 -2 -1 1 0])]
    (->> i-know
         (then thats-no-excuse)
         (then anyone-else)
         (times 2)
         (where :pitch scale/raise)
         (all :part :melody))))

(def ba-da 
  (->> (phrase [1 1 1 1/2 1 1/2 1/2 1/2 1 1/2 1/2]
               [0 0 0 -2 -3 0 -2 -3 0 -2 -3])
       (canon/canon (canon/interval -7))
       (times 8)
       (where :pitch scale/raise)
       (all :part :melody)))

(def twiddle
  (let [twid (fn [a b c] (phrase [1/2 1/2 1/2 3/2 1/2 1/2] [b a b c b a]))]
    (->> (twid -1 2 3)
         (then (twid -2 1 2)) 
         (where :pitch scale/raise)
         (after 24)
         (all :part :accompaniment))))

(def beat1
  (let [k (->> (phrase [1 1 2/3 1/3 1/3 1/3 1/3 1 1 1 1] (repeat -14))
                       (where :drum (is kick)))
        t (->> (phrase [2 2 2 1] (cycle [7 14])) (where :drum (is tip)))]
    (->> (with k t)
         (all :part :beat)
         (times 4))))

(def beat2
  (let [k (->> (phrase [1 1 1 1 1 1 1 1/2 1/2] (repeat -14))
               (where :drum (is kick)))
        t (->> (phrase [1 1 1 1/2 1/2 1 1 1/2 1/4 1/4 1/4 1/4]
                       [7 14 7 14 14 14 14 14 7 7 7 7])
               (where :drum (is tip))
               (after 1/2))]
    (->> (with k t)
         (all :part :beat)
         (times 4))))

; Track
(def intro
  (with bassline accompaniment twiddle beat2))
     
(def verse
  (->>
    intro
    (times 2)
    (with my-friend)))

(def chorus
  (->>
    (with flatline flock core-med beat1)
    (then (->> (with flatline flock core-med beat1)
               (take-while #(-> % :time (< 24)))
               (with twiddle)))))

(def bridge
  (let [bass (->> (phrase (repeat 4) (concat (range 0 7) [8])) 
                  (where :pitch (comp scale/lower scale/lower))
                  (all :part :bass))
        arpeggs (->> (phrase (cycle [2 1 1
                                     1 1 1 1])
                             [2 0 -3
                              -2 1 3 5
                              4 2 -1
                              -2 0 1 0])
                     (times 2)
                     (all :part :accompaniment))]
    (->> (with bass arpeggs core-med)
         (then (with flatline flock arpeggs core-med)))))

(def track
  (->>
    intro 
    (then verse) 
    (then chorus)
    (then verse ) 
    (then bridge)
    (then (with chorus ba-da))
    (filter #(-> % :part (not= :beat)))
    (where :pitch the-key)
    (where :time (bpm 120))
    (where :duration (bpm 120))))

(comment
  ; Loop the track, allowing live editing.
  (live/jam (var track))
  (recording-start "my-friend.wav")
  (live/play track)
  (recording-stop))
