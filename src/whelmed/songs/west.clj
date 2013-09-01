(ns whelmed.songs.west
  (:use
    [leipzig.melody]
    [leipzig.live]
    [leipzig.temperament]
    [whelmed.melody]
    [leipzig.scale]
    [leipzig.chord]
    [whelmed.instrument]))

(def progression
  (map (partial root seventh) [0 (lower 4) (lower 5) (lower 2)]))

; Accompaniment
(def backing
  (let [render-chord
         (fn [chord] (->> chord vals
           (map #(zipmap [:time :duration :pitch] [0 4 %]))))]
    (->>
      progression
      (map render-chord)
      (reduce #(then %2 %1))
      (where :part (is ::accompaniment)))))

; Lead
(def ill-run-away
  (->>
   (phrase
     [1/2 1/4 1/4 1/2]
     [  3   4   3   4])
  (after -1/2)))

(def ill-get-away
  (->> ill-run-away
    (wherever (between? 1/4 1/2), :pitch (from 3))))

(def my-heart-will-go-west-with-the-sun
  (after -1/2
     (phrase
       [1/2 3/4 3/4 2/4 3/4 3/4 1/4 17/4]
       [  3   4   3   2   4   3   2   -1])))

(def west-with-the-west-with-the 
  (let [west-with-the
          (->> my-heart-will-go-west-with-the-sun
               (take 4) (drop 1) (times 4))]
  (->>
    [{:time -1/2 :pitch 3 :duration 1/2}]
    (then west-with-the)
    (then [{:time 0 :pitch 7 :duration 1/4}]))))

(def theme
  (->>
    ill-run-away
    (then (after 3 ill-get-away))
    (then (after 3 my-heart-will-go-west-with-the-sun))
    (where :part (is ::lead))))

(def half-theme
  (let [crippled-theme
          (->> ill-run-away
               (then (->> ill-get-away (after 3))))]
    (->>
      (after 1/2 crippled-theme)
      (then (after 3 crippled-theme))
      (where :part (is ::lead)))))

(def spilling-theme
  (->>
    ill-run-away
    (then (after 3 ill-get-away))
    (then (after 3 west-with-the-west-with-the))
    (where :part (is ::lead))))

; Response
(def a-parting-kiss
  (phrase
    [1/4 1/4 1/4 3/4 10/4]
    [  4   3   4   6    4]))

(def like-fairy-floss
  (with [{:time -1/4 :pitch 3 :duration 1/4}]
        a-parting-kiss))

(def dissolves-on-the-tip-of-my-tongue
  (->>
    (phrase
      [1/4 3/4 13/4]
      [  4   6    4])
    (after -1/4)))

(def reply
 (->>
   a-parting-kiss
   (then like-fairy-floss)
   (then dissolves-on-the-tip-of-my-tongue) 
   (then dissolves-on-the-tip-of-my-tongue)
   (where :part (is ::response))))

; Break
(def consider-this
  (after -3/2
     (phrase
       [1/2 1/2 1/2 8/2]
       [  4   9   8   7])))

(def consider-that
  (->> consider-this
    (wherever (between? 0 1/2), :pitch dec)))

(def consider-everything
  (->>
    (take 3 consider-this)
    (then
      (phrase
        [2/2 1/2 2/2 2/2 9/2]
        [  7   8   7   6   4]))))

(def breakdown
 (->>
   consider-this
   (then consider-that)
   (then consider-everything)))

(def breakup (->> breakdown (where :pitch lower)))
(def break
  (->>
    (with breakup breakdown)
    (where :part (is ::break))))

; Bass
(def light-bass
  (->> (map :i progression)
    (phrase (repeat 4))
    (where :pitch lower)
    (where :part (is ::bass))))

(def bass
  (->> light-bass
    (with (->> light-bass
            (where :pitch (from 6))
            (where :time inc)
            (where :duration dec)))))

; Body
(def west-with-the-sun
  (let [accompaniment
          (->> backing (with bass)) 
        intro
         (->> backing (then accompaniment))
        call
          (->> theme (with accompaniment) (times 2))
        response
          (->> reply (with accompaniment) (times 2))
        variation
          (->> theme (then spilling-theme)
            (with (->> accompaniment (times 2))))
        fadeout
          (->> accompaniment (with half-theme) (then bass))]
    (->>
      intro (then call) (then response)
      (then (->> break (with light-bass) (times 2)
              (with (->> backing (after 16)))))
      (then variation)
      (then (->> response (with (->> break (after 16)))))
      (then fadeout)
      (where :pitch (comp equal A minor))
      (where :time (bpm 80))
      (where :duration (bpm 80)))))

; Arrangement
(defmethod play-note ::bass [{freq :pitch}] (-> freq groan))
(defmethod play-note ::accompaniment [{freq :pitch}] (-> freq shudder))
(defmethod play-note ::lead [{freq :pitch}] (-> freq sawish))
(defmethod play-note ::response [{freq :pitch ms :duration}] (-> freq (organ ms 3)))
(defmethod play-note ::break [{freq :pitch}] (-> freq (bell 2000 8)))

(comment
  (->> west-with-the-sun play)
) 
