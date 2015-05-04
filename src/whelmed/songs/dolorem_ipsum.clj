(ns whelmed.songs.dolorem-ipsum
  (:use
    [leipzig.melody]
    [leipzig.live]
    [whelmed.melody]
    [leipzig.scale]
    [leipzig.chord]
    [leipzig.canon]
    [whelmed.instrument])
  (:require [leipzig.temperament :as temperament]))

; Extra concepts
(defn arpeggiate [chord ks duration]
  (map 
    (fn [k time] {:time time :pitch (chord k) :duration duration})
    ks
    (reductions + 0 (repeat duration))))

(def sixth (-> triad (assoc :vi 5)))

; Melody
(def neque
  (->>
    (phrase
      [1/2 1/2 1/2 1/4 1/4 1/2 1/2 1/2 1/4 1/4]
      [4 4 5 4 5 6 8 5 4 5])
    (times 2)
    (but 3.5 4 (phrase [1/8 1/8 1/8 1/8] [4 5 4 5]))
    (all :part ::melody)))

(def sit-amet 
  (->>
    (phrase
      [4 1 3]
      [4 6 5])
    (all :part ::melody)))

(def notice
  (->>
    (phrase
      [1 5/2 1/4 1/4 2 2 4 1 2.5 1/4 1/4 4 4]
      [5 4 2 3 4 7 6 6 5 4 3 4 2.5])
    (all :part ::melody)))

(def it (->> (reduce with
                     [(phrase [1] [7]) (phrase [2] [4]) (phrase [3] [0])])
             (all :part ::melody)))

; Arpeggios
(def theme 
  (let [up
          #(-> % (augment :iii 1) (augment :v 1))
        chords
          [triad (up triad) (up (up triad)) (up triad)]]
    (->> chords
      (map #(arpeggiate % [:v :i :iii :v] 1/4))
      (reduce #(then %2 %1))
      (times 2)
      (all :part ::arpeggios))))

(def response
  (->>
    (arpeggiate (-> triad (root 4) (inversion 2))
                [:i :v :i :iii] 1/4)
    (times 4)
    (then
      (->> (arpeggiate (-> sixth (root 1))
                       [:v :iii :i :vi] 1/4)
           (times 4)))
    (all :part ::arpeggios)))

(def wander
  (->> 
      (arpeggiate (-> triad (root 2))
        [:iii :i :iii :v] 1/4)
      (times 4)
      (wherever (between? 15/4 16/4), :pitch inc)
    (then
      (->> (arpeggiate (-> sixth (root 2))
             [:v :iii :i :vi] 1/4)
        (times 4)))
    (then
      response)
    (then
      (->> (arpeggiate (-> triad (root 4) (inversion 2))
             [:i :v :i :iii] 1/4)
        (times 4)))
    (then
      (->> (arpeggiate
             (-> triad (root 4) (inversion 2) (augment :i -3/2))
             [:i :v :i :iii] 1/4)
        (times 4)))
    (all :part ::arpeggios)))

(def air
  (->>
    (map #(times 4 (phrase [1/4] [%])) [0 -4 0 -5 0 0 0])
    (reduce #(then %2 %1))
    (all :part ::arpeggios)))

(def ends (->> (phrase [1/4] [7]) (all :part ::arpeggios)))

; Oooh
(def aaah
  (->> (phrase [1 1] [6 5]) (times 4)
       (all :part ::oooh)))

(def oooh-aaah
  (->>
    (phrase
      [1 1 1 1]
      [2 3 4 3])
    (canon (interval -5))
    (with (phrase [1 1 1 1] [0 0 0 0]))
    (times 2)
    (then
      (->> aaah (with (phrase [2 2 2 2] [4 4 3 3]))
        (with (phrase [1 1 1 1] [1 1 1 1]))))
    (all :part ::oooh)
    (with (->> (phrase (repeat 16 1/2) (repeat 7))
               (then (phrase (repeat 8 1/2) (repeat 4)))
               (then (phrase (repeat 8 1/2) (repeat 1)))
               (all :part ::arpeggios)))))

(def la-la-la-la
  (->>
    (phrase [2 1 1/2 1/2 2 1 1/2 1/2 4]
            [4 8 6 4 2 8 6 4 1])
    (then (phrase [4] [3]))
    (times 2)
    (all :part ::oooh)))

(def wa-wa-wa-wa
  (->>
    (phrase [4 4 4 4 4 4] [4 7 8 10 11 8])
    (all :part ::oooh)))

; Pull it all together
(def dolorem-ipsum
  (let [lorem
          (->> theme (then response))
        intro
          (->> lorem (with (->> neque (then sit-amet))) (times 2))
        development
          (->> wander (with notice))
        finale
          (with it ends)]
    (->> lorem
      (then intro)
      (then development)
      (then (->> theme (wherever (between? 4 8), :pitch raise)))
      (then (->> theme (with neque)))
      (then oooh-aaah)
      (then (->> intro (with la-la-la-la)
              (then (with development wa-wa-wa-wa))
              (then air) (then finale)))
      (in-time (bpm 80))
      (where :pitch (comp temperament/equal F lydian)))))

; The arrangement
(defmethod play-note ::melody [{:keys [pitch duration]}]
  (some-> pitch (bell (* 7 duration) :position 1/8 :wet 0.5 :volume 0.4))
  (some-> pitch (bell (* 8 duration) :position 1/9 :wet 0.9 :room 0.2 :volume 0.25)))
(defmethod play-note ::arpeggios [{:keys [pitch duration]}]
  (some-> pitch (/ 2) (brassy duration 0.3 0.1 :noise 9 :pan -1/3 :p 3/3 :wet 0.4 :vol 0.2 :p 8/6))
  (some-> pitch (/ 2) (corgan 0.5 :depth 0.3 :walk 0.3 :pan 1/3 :wet 0.6 :vol 0.4 :room 0.5)))
(defmethod play-note ::oooh [{:keys [pitch duration]}]
  (some-> pitch (groan (* 2 duration) :low 10 :vibrato 8/3 :position -1/6 :volume 0.1)))

(comment
  (->> dolorem-ipsum play)
  (jam (var dolorem-ipsum))
)
