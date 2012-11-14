(ns whelmed.songs.west
  (:use
    [leipzig.melody]
    [whelmed.melody]
    [leipzig.scale]
    [leipzig.chord]
    [whelmed.instrument]
    [overtone.live :only [stop midi->hz]]))

(def progression
  (map (partial root seventh) [0 (low 4) (low 5) (low 2)]))

(def backing
  (let [render-chord (fn [start notes] (map #(identity {:time start :duration 4 :pitch %}) notes))]
    (->>
      progression
      (map vals)
      (map render-chord [0 4 8 12])
      (reduce with)
      (where :part (is ::accompaniment)))))

(def ill-run-away
  (->>
   (phrase
     [1/2 1/4 1/4 1/2]
     [  3   4   3   4])
  (after -1/2)))

(def ill-get-away (assoc (vec ill-run-away) 2 {:time 1/4 :pitch 6 :duration 1/4}))

(def my-heart-will-go-west-with-the-sun
  (after -1/2
     (phrase
       [1/2 3/4 3/4 2/4 3/4 3/4 1/4 17/4]
       [  3   4   3   2   4   3   2   -1])))

(def west-with-the-west-with-the 
  (let [west-with-the (->>
                        my-heart-will-go-west-with-the-sun
                        (cut 1 4)
                        (times 4))]
  (->>
    [{:time -1/2 :pitch 3 :duration 1/2}]
    (then west-with-the)
    (then [{:time 0 :pitch 7 :duration 1/4}]))))

(def a-parting-kiss
  (phrase
    [1/4 1/4 1/4 3/4 10/4]
    [  4   3   4   6    4]))

(def like-fairy-floss (cons {:time -1/4 :pitch 3 :duration 1/4} a-parting-kiss))

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

(def consider-this
  (after -3/2
     (phrase
       [1/2 1/2 1/2 8/2]
       [  4   9   8   7])))

(def consider-that (assoc (vec consider-this) 3 {:time 0 :pitch 6 :duration 4})) 
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

(def breakup (->> breakdown (where :pitch low)))
(def break
  (->>
    (with breakup breakdown)
    (where :part (is ::break))))

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

(def light-bass
  (->> (map :i progression)
    (phrase (repeat 4 4))
    (where :pitch low)
    (where :part (is ::bass))))

(def bass
  (->> light-bass
    (with (->> light-bass
            (where :pitch #(+ 6 %))
            (where :time inc)
            (where :duration dec)))))

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
      (where :pitch (comp E minor))
      (where :time (bpm 80))
      (where :duration (bpm 80)))))

(defmethod play-note ::bass [{:keys [pitch]}] (-> pitch midi->hz groan))
(defmethod play-note ::accompaniment [{:keys [pitch]}] (-> pitch midi->hz shudder))
(defmethod play-note ::lead [{:keys [pitch]}] (-> pitch midi->hz sawish))
(defmethod play-note ::response [{:keys [pitch]}] (-> pitch midi->hz sinish))
(defmethod play-note ::break [{:keys [pitch]}] (-> pitch midi->hz sinish))

;(->> west-with-the-sun play)
