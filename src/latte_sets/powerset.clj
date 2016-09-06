(ns latte-sets.powerset

  "Notions about the powerset construction.

  In the predicate-as-set encoding of set-theoretic notions,
 the powerset construction (i.e. building a set of sets) is
not immediate. The reason is that the set constructor `(set T)'
 is not itself a type (but a kind). Hence we need to replicate
 some part of the type theory (e.g. the existential quantifier) 
to deal with powersets."

    (:refer-clojure :exclude [and or not set])

    (:require [latte.core :as latte :refer [definition defthm defaxiom defnotation
                                            forall lambda ==>
                                            assume have proof lambda]]
              [latte.quant :as q :refer [exists]]
              [latte.prop :as p :refer [<=> and or not]]
              [latte.equal :as eq :refer [equal]]

              [latte-sets.core :as s :refer [set elem seteq subset]]))

(definition powerset
  "The powerset constructor. 

The term `(powerset T)' is the type 
of sets whose elements are sets of type `T`."
  [[T :type]]
  (==> (set T) :type))

(definition set-elem
  "Membership for powersets.
Th set `x` is an element of the powerset `X`."
  [[T :type] [x (set T)] [X (powerset T)]]
  (X x))

(definition set-ex
  "The powerset existential.
This is the definition of [[latte.quant/ex]] but
adpated for sets."
  [[T :type] [X (powerset T)]]
  (forall [α :type]
    (==> (forall [x (set T)]
           (==> (set-elem T x X) α))
         α)))

(defthm set-ex-elim
  "The elimination rule for the set existential."
  [[T :type] [X (powerset T)] [A :type]]
  (==> (set-ex T X)
       (forall [x (set T)]
         (==> (set-elem T x X) A))
       A))

(proof set-ex-elim :script
  (assume [H1 (set-ex T X)
           H2 (forall [x (set T)] (==> (set-elem T x X) A))]
    (have a (==> (forall [x (set T)]
                   (==> (set-elem T x X) A))
                 A) :by (H1 A))

    (have b A :by (a H2))
    (qed b)))

(defthm set-ex-intro
  "Introduction rule for [[ex-set]]."
  [[T :type] [X (powerset T)] [x (set T)]]
  (==> (set-elem T x X)
       (set-ex T X)))

(proof set-ex-intro
    :script
  (assume [H (set-elem T x X)
           A :type
           Q (forall [y (set T)] (==> (set-elem T y X) A))]
    (have a (==> (set-elem T x X) A) :by (Q x))
    (have b A :by (a H))
    (have c _ :discharge [A Q b])
    (qed c)))

(definition set-single
  "The powerset version of [[latte.quant/single]].
There exists at most one set ..."
  [[T :type] [X (powerset T)]]
  (forall [x y (set T)]
    (==> (set-elem T x X)
         (set-elem T y X)
         (seteq T x y))))

(definition set-unique
  "The powerset version of [[latte.quant/unique]].
There exists a unique set ..."
  [[T :type] [X (powerset T)]]
  (and (set-ex T X)
       (set-single T X)))

(defaxiom the-set
  "The powerset version of [[latte.quant/the]]."
  [[T :type] [X (powerset T)] [u (set-unique T X)]]
  (set T))

(defaxiom the-set-prop
  "The property of the unique set descriptor [[the-set]]."
  [[T :type] [X (powerset T)] [u (set-unique T X)]]
  (set-elem T (the-set T X u) X))

(defthm the-set-lemma
  "The unique set ... is unique."
  [[T :type] [X (powerset T)] [u (set-unique T X)]]
  (forall [y (set T)]
    (==> (set-elem T y X)
         (seteq T y (the-set T X u)))))

(proof the-set-lemma
    :script
  (have a (set-single T X) :by (p/%and-elim-right u))
  (have b (set-elem T (the-set T X u) X) :by (the-set-prop T X u))
  (assume [y (set T)
           Hy (set-elem T y X)]
    (have c (==> (set-elem T y X)
                 (set-elem T (the-set T X u) X)
                 (seteq T y (the-set T X u))) :by (a y (the-set T X u)))
    (have d (seteq T y (the-set T X u)) :by (c Hy b))
    (have e _ :discharge [y Hy d]))
  (qed e))


(definition unions
  "Generalized union.
This is the set {y:T | ∃x∈X, y∈x}."
  [[T :type] [X (powerset T)]]
  (lambda [y T]
    (set-ex T (lambda [x (set T)]
                (and (set-elem T x X)
                     (elem T y x))))))

;; TODO
;; (defthm unions-lower-bound
;;    "The generalized union is a lower bound wrt. 
;; the subset relation."
;;    [[T :type] [X (powerset T)]]
;;    (forall [x (set T)]
;;      (==>  (set-elem T x X)
;;            (subset T (unions T X) x))))

;; (proof unions-lower-bound
;;     :script
;;   (assume [x (set T)
;;            Hx (set-elem T x X)]
;;     (assume [y T
;;              Hy (elem T y (unions T X))]
;;       (have a (set-ex T (lambda [z (set T)]
;;                           (and (set-elem T z X)
;;                                (elem T y z)))) :by Hy)
;;       (have b (==> (forall [z (set T)]
;;                      (==> (and (set-elem T z X)
;;                                (elem T y z))
;;                           (elem T y x)))
;;                    (elem T y x))
;;             :by ((set-ex-elim T (lambda [z (set T)]
;;                                   (and (set-elem T z X)
;;                                        (elem T y z)))
;;                               (elem T y x)) a)))))

(definition intersections
  "Generalize intersections.
This is the set {y:T | ∀x∈X, y∈x}."
  [[T :type] [X (powerset T)]]
  (lambda [y T]
    (forall [x (set T)]
      (==> (set-elem T x X)
           (elem T y x)))))

;; (defthm intersection-upper-bound
;;   "The generalized intersection is an upper bound wrt. the subset relation."
;;   [[T :type] [X (powerset T)]]
;;   (forall [x (set T)]
;;     (==> (set-elem T x X)
;;          (subset T x (intersections T X)))))

;; (proof intersection-upper-bound
;;     :script
;;   (assume [x (set T)
;;            Hx (set-elem T x X)]
;;     (assume [y T
;;              Hy (elem T y x)]
;;       (assume [z (set T)
;;                Hz (set-elem T z X)]
;;         (have a )))))




