(ns whelmed.songs.sidhe
  (:use
    [leipzig.melody]
    [leipzig.live]
    [leipzig.canon]
    [whelmed.melody]
    [leipzig.scale]
    [leipzig.chord]
    [whelmed.instrument])
  (:require [overtone.live :as overtone]
            [overtone.inst.drum :as drums]
            [overtone.synth.stringed :as strings]))
(def beat 
  (->>
    [(->> (rhythm [3/2 1/2 1/2 3/2]) (where :drum (is :kick)))
     (->> (rhythm [2 1]) (after 1) (where :drum (is :tock)))
     (->> (rhythm [1/2]) (after 7/2) (where :drum (is :tick)))]
    (reduce with)
    (times 8)
    (all :part ::beat)))

(def bassline
  (->>
    (phrase (repeat 4) (range 0 -8 -1))
    (where :pitch lower)
    (all :part ::bass)))

(def intro
  (let [vectorise (fn [chord] (-> chord vals sort vec))
        twiddle
        (fn [chord]
          (->> (phrase (repeat 5 1/4) (repeat chord))
               (then (phrase (mapcat repeat [3 4] [1/4 1/2])
                             (map chord [0 1 2 3 2 1 0]))) 
               (all :part ::melody)))
        strum (fn [chord] (phrase (repeat 4 1) (repeat chord)))
        bonus (-> triad (root -3) (inversion 2) (augment :iii 1/2))
        ]
    (->>
      (reduce #(then %2 %1)
              [(twiddle (-> triad (inversion 2) (assoc :extra 1) vectorise))
               (strum (-> triad (root -3)))
               (twiddle (-> triad (root -4) (assoc :extra -1) vectorise))
               (phrase (mapcat repeat [2 8] [1 1/4])
                       (mapcat repeat [2 4 4] [bonus (augment bonus :iii 1/2) bonus]))
               (twiddle (-> triad (root -4) (inversion 2) (assoc :extra -3) vectorise))
               (twiddle (-> triad (root -5) (inversion 2) (assoc :extra -4) vectorise))
               (twiddle (-> triad (root -3) (inversion 1) (assoc :extra -4) vectorise))
               (strum (-> triad (inversion 2) (root -7)))])
      (wherever (comp not :part) :part (is ::intro)))))

(def flourishes 
  (let [first-flourish (phrase
                         [1/4 1/4 3/2 1 1 9/2]
                         [1 2 3 4 2 1])
        second-flourish (phrase (map :duration first-flourish)
                                [1 2 3 2 1 0])]
    (->>
      (phrase [5/2 1/2 1/2 8/2] [4 3 2 4])
      (then first-flourish)
      (then (phrase [5/2 1/4 1/4 9/2] [4 2 3 4]))
      (then second-flourish) 
      (all :part ::default))))

(def harmony
  (->> bassline
       (where :pitch (from 9))
       (wherever #(-> % :time (= 12)) :pitch (from 1/2))
       (all :part ::bass)))

(def melody
  (->>
    (phrase
      [1/2 1 1/2 2 1/2 1 1/2 2 1/2 1/2 1/2 1 1 1/2 4]
      [-4 0 0 0 2 4 5 4 4 4 3 2 2 3 4])
    (then
      (phrase
        [1/2 1/2 1 2 1/2 1/2 1 2]
        [4 4 3 2 3 4 6 4]))
    (then
      (phrase
        [1/2 3/2 1/2 3/2 1/2 7/2]
        [2 1 2 1 -1 0]))
    (after -1/2)
    (where :part (is ::default))))

(def lead-in
  (->>
    (phrase
      (repeat 1/2)
      [-3 0 2 1 0])
    (after -3)
    (all :part ::default)))

(def progression
  {:main
   [(-> ninth (root 0))
    (-> seventh (root 4))
    (-> seventh (root 3))
    (-> triad (root 4) (augment :iii 1/2))
    (-> seventh (root 3))
    (-> triad (root 2))
    (-> seventh (root 4))
    (-> seventh (root 0))]
   :transition
   [(-> triad (inversion 2))
    (-> triad (root -3))
    (-> triad (root -4))]
   :emphasis
   [(-> triad (root -1) (inversion 2))
    (-> triad (root -3/2) (inversion 2))
    (-> triad (root 1) (inversion 1) (augment :v 1/2))]})

(def chords
  (->>
    (:main progression)
    (phrase (repeat 4))
    (where :pitch raise)
    (all :part ::chords)))

(def fall-down
  (let [beat (->>
               (rhythm (repeat 32 1/2))
               (having :drum (cycle [:kick :click :click
                                     :kick :click :click
                                     :kick :click]))
               (where :part (is ::beat))
               (after 16))]
    (->>
      (phrase (cycle [4 4 8])
              [0 -3 -4 -1 -3/2 1])
      (where :pitch lower)
      (where :part (is ::bass))
      (with (->>
              (phrase [1 3 1 3 1 7 1 3 1 3 1 8]
                      (interleave [0 3 -1 0 -1 -3/2]
                                  (concat (:transition progression) (:emphasis progression))))
              (after -1)))
      (where :part (is ::default))
      (with beat) 
      )))

(def emphasis
  (let [melody
        (phrase [3 1/2 1/2 2 2 2 2 4] [3 4 3 2.5 0.5 5.5 4 3])
        beat (->> (cycle [1/2 7 1/2]) rhythm (all :drum :kick)
                  (all :part ::beat)
                  (take 23))]
    (->> (:emphasis progression)
         (phrase [4 4 8])
         (times 2)
         (where :part (is ::chords))
         (with (->> melody (then (->> melody (drop-last 3) (then (phrase [4] [1]))))
                    (all :part ::default)))
         (times 2)
         (with beat))))

(def breakdown
  (let [dux-a
        (phrase
          (mapcat repeat [8 1 1 4 1] [1 3/2 1/2 1 3/2])
          [0 1 2 0 1 2 3 1 3 3 2 1 0 -1/2 1])
        dux-b
        (phrase
          [1/2 3/2 1/2 1 1 1 1 3/2 1/2 3/2 1/2 3/2 1/2 1 1 2]
          [2 3 3 2 1 0 -1 -3 -1 -3 -1 -3 -1 -3 -5 -7])
        comes-a
        (after 4
               (phrase
                 [1 1 1 1 3/2 1/2 1/2 1/2 1/2 1/2 7/2]
                 [-1 0 1 -1 -2 -3 -4 -2 0 3 1]))
        comes-b
        (phrase (concat (repeat 25 1/2) [1 1 1/2 1/2 1/2 1/2])
                [1 -4 -2 0 3 -4 -2 0 3 -5 -3 -1 2 -5 -3 -1 2 -3 -1 1 -3 1 2 1 -1 0 2 1 0 -1 -3])
        ]
    (->>
      dux-a
      (then dux-b)
      (with (->> comes-a (then comes-b)) )
      (times 2)
      (with (after 32 bassline))
      (all :part ::melody))))

(def kit {:kick drums/kick2 
          :tick drums/closed-hat,
          :tock drums/open-hat
          :click click})

(defmethod play-note ::beat [note] ((-> note :drum kit)))
(defmethod play-note ::default [note] (pick 0.99 0.1 note))
(defmethod play-note ::intro [{midi :pitch seconds :duration}]
  (organ (overtone/midi->hz midi) seconds 0.7))
(defmethod play-note ::bass [note]
  (-> note (assoc :part ::default) play-note))
(defmethod play-note ::melody [note]
  (-> note (assoc :part ::default) play-note))
(defmethod play-note ::chords [{midi :pitch seconds :duration}]
  (organ (overtone/midi->hz midi) seconds 0.5))

(def piece 
  (->>
    breakdown
    (then (with bassline harmony flourishes))
    (then (with beat bassline harmony flourishes chords))
    (then (with bassline harmony lead-in melody))
    (then (with bassline harmony melody beat))
    (then fall-down)
    (then emphasis)
    (where :pitch (comp C minor))
    (in-time (bpm 105))))

(def sidhe
  (mapthen
    (fn [round] (wherever :pitch, :pitch #(+ % (* 2 round)) piece))
    (range 0 2)))

(def sidhe-sparse
  (->> 
    (with intro bassline)
    (then (with intro bassline beat))
    (then fall-down)
    (then emphasis)
    (where :pitch (comp B flat minor))
    (then (->> breakdown (then (phrase [64] [0]))
               (where :pitch (comp C minor))
               (where :part (is ::melody))))
    (in-time (bpm 80))))
