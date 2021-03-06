(ns myspace (:require [arcadia.core :as a]
                      [arcadia.linear :as l])
    (:import UnityEngine.Rigidbody)
    (:import UnityEngine.Transform))

(a/log "myspace loaded")

;;;;player controller re-write;;;;;;;;;;;
;;default values:
;;speed 10
;;tilt 3
;;xMin -6.5 xMax 6.5 zMin -4 zMax 8
;;shot is a bolt prefab
;;shot spawn is a transform
;;fire rate 0.25
;;default values hardcoded for brevity

(defn move-player [& args]
  (let [move-horizonal (Input/GetAxis "Horizontal")
        move-vertical (Input/GetAxis "Vertical")
        movement (vector move-horizonal 0.0 move-vertical)
        speed (fn [x] (* 10 x))
        velocity   (apply l/v3 (mapv speed movement))
        tilt 3
        player (a/object-named "player")
        rigid-body (a/cmpt player Rigidbody)
        rigid-body-position (.position rigid-body)
        x-min -6.5
        x-max 6.5
        z-min -4
        z-max 8.0
        x-boundary (Mathf/Clamp (.x rigid-body-position) x-min x-max)
        z-boundary (Mathf/Clamp (.z rigid-body-position) z-min z-max)
        ]

    (set! (.velocity rigid-body) velocity)
    (set! (.position rigid-body) (l/v3 x-boundary 0.0 z-boundary))
    (set! (.rotation rigid-body) (l/euler (l/v3 0.0 0.0 (* (* -1 tilt) (.x (.velocity rigid-body))))) )
    ))

;hook the moveplayer fn to the fixed update event
(a/hook+ (a/object-named "player")  :fixed-update #'myspace/move-player)


;;lets give this guy a weapon
(def next-fire-time (atom (Time/time)))
(def firing-rate (atom 0.55))
(defn fire-lazer [&args]
  (let [now (Time/time)
        shot-spawn (a/cmpt (a/object-named "player") Transform)
        ]
    (when (and (Input/GetButton "Fire1")
               (> now (deref next-fire-time)))
      (let [bolt (a/instantiate (UnityEngine.Resources/Load "Bolt") (.position (.transform (a/object-named "Shot Spawn"))))
      rb (a/cmpt bolt Rigidbody)
            new-velocity (l/v3* (.forward (a/cmpt bolt Transform)) 20)
            _ (swap! next-fire-time + @firing-rate)
      ]
        (set! (.velocity rb) new-velocity )))))

;;hook the weapon up to the player
(a/hook+ (a/object-named "player")  :update #'myspace/fire-lazer)

(defn say [& _] (a/log "Oh Helloo..."))

(let [bolt (a/instantiate (UnityEngine.Resources/Load "Bolt") (.position (.transform (a/object-named "Shot Spawn"))))
      rb (a/cmpt bolt Rigidbody)
      new-velocity (l/v3* (.forward (a/cmpt bolt Transform)) 20)
      ]
    (set! (.velocity rb) new-velocity ))
(mapv a/destroy (a/objects-named #"Bolt*"))
(+ 1 1)
(a/hook+
 (a/instantiate (UnityEngine.Resources/Load "Bolt") (.position (.transform (a/object-named "Shot Spawn")))) :start  )
(a/hook+  (a/objects-named  #".*Bolt.*") :start #'myspace/say)
