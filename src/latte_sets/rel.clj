(ns latte-sets.rel
  "A **relation** from elements of
a given type `T` to elements of `U` is formalized with type `(==> T U :type)`.

  This namespace provides some important properties about such
  relations."

  (:refer-clojure :exclude [and or not identity set])

  (:require [latte.core :as latte :refer [definition defaxiom defthm defimplicit
                                          deflemma forall lambda
                                          proof assume have pose qed]]
            [latte-prelude.prop :as p :refer [and or not <=>]]
            [latte-prelude.equal :as eq :refer [equal]]
            [latte-prelude.quant :as q :refer [exists]]
            [latte-sets.core :as sets :refer [set elem]]))

(definition rel
  "The type of relations."
  [[T :type] [U :type]]
  (==> T U :type))

(defn fetch-rel-type [def-env ctx r-type]
  "Fetch the `T` and `U` in a relation-type `r-type` of the form `(rel T U)` (fails otherwise).
This function is used for implicits in relations."
  (let [[T cod-type] (p/decompose-impl-type def-env ctx r-type)]
    (let [[U _] (p/decompose-impl-type def-env ctx cod-type)]
      [T U])))

(definition dom-def
  "The domain of relation `R`."
  [[T :type] [U :type] [R (rel T U)]]
  (lambda [x T]
          (exists [y U] (R x y))))

(defimplicit dom
  "`(dom R)` is the domain of relation `R`, cf. [[dom-def]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'dom-def T U R)))

(definition ran-def
  "The range of relation `R`."
  [[T :type] [U :type] [R (rel T U)]]
  (lambda [y U]
          (exists [x T] (R x y))))

(defimplicit ran
  "`(ran R)` is the range (or codomain) of relation `R`, cf. [[ran-def]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'ran-def T U R)))

(definition identity
  "The indentity relation on `T`."
  [[T :type]]
  (lambda [x y T]
    (equal x y)))

(definition reflexive-def
  "A reflexive relation."
  [[T :type] [R (rel T T)]]
  (forall [x T] (R x x)))

(defimplicit reflexive
  "`(reflexive R)` holds if `R` is reflexive, cf. [[reflexive-def]]."
  [def-env ctx [R R-ty]]
  (let [[T _] (fetch-rel-type def-env ctx R-ty)]
    (list #'reflexive-def T R)))

(defthm ident-refl
  [[T :type]]
  (reflexive (identity T)))

(proof 'ident-refl
  (assume [x T]
    (have <a> (equal x x) :by (eq/eq-refl x)))
  (qed <a>))

(definition symmetric-def
  "A symmetric relation."
  [[T :type] [R (rel T T)]]
  (forall [x y T]
    (==> (R x y)
         (R y x))))

(defimplicit symmetric
  "`(symmetric R)` holds if `R` is symmetric, cf. [[reflexive-def]]."
  [def-env ctx [R R-ty]]
  (let [[T _] (fetch-rel-type def-env ctx R-ty)]
    (list #'symmetric-def T R)))

(defthm ident-sym
  [[T :type]]
  (symmetric (identity T)))

(proof 'ident-sym
  (assume [x T
           y T
           Hx ((identity T) x y)]
    (have <a> (equal x y) :by Hx)
    (have <b> (equal y x) :by (eq/eq-sym <a>)))
  (qed <b>))

(definition transitive-def
  "A transitive relation."
  [[T :type] [R (rel T T)]]
  (forall [x y z T]
    (==> (R x y)
         (R y z)
         (R x z))))

(defimplicit transitive
  "`(transitive R)` holds if `R` is transitive, cf. [[transitive-def]]."
  [def-env ctx [R R-ty]]
  (let [[T _] (fetch-rel-type def-env ctx R-ty)]
    (list #'transitive-def T R)))

(defthm ident-trans
  [[T :type]]
  (transitive (identity T)))

(proof 'ident-trans
  (assume [x T
           y T
           z T
           H1 ((identity T) x y)
           H2 ((identity T) y z)]
    (have <a> (equal x y) :by H1)
    (have <b> (equal y z) :by H2)
    (have <c> (equal x z) :by (eq/eq-trans <a> <b>)))
  (qed <c>))

(definition equivalence-def
  "An equivalence relation."
  [[T :type] [R (rel T T)]]
  (and (reflexive R)
       (and (symmetric R)
            (transitive R))))

(defimplicit equivalence
  "`(equivalence R)` holds if `R` is an equivalence relation, cf. [[equivalence-def]]."
  [def-env ctx [R R-ty]]
  (let [[T _] (fetch-rel-type def-env ctx R-ty)]
    (list #'equivalence-def T R)))

(defthm ident-equiv
  "The indentity on `T` is an equivalence relation."
  [[T :type]]
  (equivalence (identity T)))

(proof 'ident-equiv
  (qed (p/and-intro (ident-refl T)
                    (p/and-intro (ident-sym T)
                                 (ident-trans T)))))

(definition fullrel
  "The full (total) relation between `T` and `U`."
  [[T :type] [U :type]]
  (lambda [x T]
    (lambda [y U] p/truth)))

(defthm fullrel-prop
  [[T :type] [U :type]]
  (forall [x T]
    (forall [y U]
      ((fullrel T U) x y))))

(proof 'fullrel-prop
  (assume [x T
           y U]
    (have <a> ((fullrel T U) x y) :by p/truth-is-true))
  (qed <a>))

(definition emptyrel
  "The empty relation."
  [[T :type] [U :type]]
  (lambda [x T]
    (lambda [y U]
      p/absurd)))

(defthm emptyrel-prop
  [[T :type] [U :type]]
  (forall [x T]
    (forall [y U]
      (not ((emptyrel T U) x y)))))

(proof 'emptyrel-prop
  (assume [x T
           y U
           H ((emptyrel T U) x y)]
    (have <a> p/absurd :by H))
  (qed <a>))

(definition subrel-def
  "The subset ordering for relations."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (forall [x T]
    (forall [y U]
      (==> (R1 x y)
           (R2 x y)))))

(defimplicit subrel
  "The subset ordering for relations, `(subrel R1 R2)` is `R1`⊆`R2`, cf. [[subrel-def]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'subrel-def T U R1 R2)))

(defthm subrel-refl-thm
  [[T :type] [U :type] [R (rel T U)]]
  (subrel R R))

(proof 'subrel-refl-thm
  (assume [x T
           y U
           H1 (R x y)]
    (have <a> (R x y) :by H1))
  (qed <a>))

(defimplicit subrel-refl
  "`(subrel-refl R)`
Reflexivity of `subrel`, cf. [[subrel-refl-thm]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'subrel-refl-thm T U R)))

(defthm subrel-trans-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)] [R3 (rel T U)]]
  (==> (subrel R1 R2)
       (subrel R2 R3)
       (subrel R1 R3)))

(proof 'subrel-trans-thm
  (assume [H1 (subrel R1 R2)
           H2 (subrel R2 R3)]
    (assume [x T
             y U]
      (have <a> (==> (R1 x y) (R2 x y)) :by (H1 x y))
      (have <b> (==> (R2 x y) (R3 x y)) :by (H2 x y))
      (have <c> (==> (R1 x y) (R3 x y))
            :by (p/impl-trans <a> <b>))))
  (qed <c>))

(defimplicit subrel-trans
  "`(subrel-trans R1 R2 R3)`
Transitivity of `subrel`, cf. [[subrel-trans-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty] [R3 R3-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'subrel-trans-thm T U R1 R2 R3)))

(defthm subrel-prop
  "Preservation of properties on relational subsets."
  [[T :type] [U :type] [P (==> T U :type)] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (forall [x T]
         (forall [y U]
           (==> (R2 x y)
                (P x y))))
       (subrel R1 R2)
       (forall [x T]
         (forall [y U]
           (==> (R1 x y)
                (P x y))))))

(proof 'subrel-prop
  (assume [H1 (forall [x T]
                      (forall [y U]
                              (==> (R2 x y)
                                   (P x y))))
           H2 (subrel R1 R2)]
    (assume [x T
             y U
             Hxy (R1 x y)]
      (have <a> (R2 x y) :by (H2 x y Hxy))
      (have <b> (P x y) :by (H1 x y <a>))))
  (qed <b>))

(defthm subrel-emptyrel-lower-bound
  "The empty relation is a subset of every other relations."
  [[T :type] [U :type] [R (rel T U)]]
  (subrel (emptyrel T U) R))

(proof 'subrel-emptyrel-lower-bound
  (assume [x T
           y U
           Hxy ((emptyrel T U) x y)]
    (have <a> p/absurd :by Hxy)
    (have <b> (R x y) :by (<a> (R x y))))
  (qed <b>))

(defthm subrel-fullrel-upper-bound
  "The full relation is a superset of every other relations."
  [[T :type] [U :type] [R (rel T U)]]
  (subrel R (fullrel T U)))

(proof 'subrel-fullrel-upper-bound
  (assume [x T
           y U
           Hxy (R x y)]
    (have <a> ((fullrel T U) x y)
          :by p/truth-is-true))
  (qed <a>))


(definition releq-def
  "Subset-based equality on relations."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (and (subrel R1 R2)
       (subrel R2 R1)))

(defimplicit releq
  "`(releq R1 R2)` means relations `R1` and `R2` are equal wrt. the [[subrel]] ordering,
 cf. [[releq-def]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'releq-def T U R1 R2)))

(defthm releq-refl-thm
  [[T :type] [U :type] [R (rel T U)]]
  (releq R R))

(proof 'releq-refl-thm
  (have <a> (subrel R R) :by (subrel-refl R))
  (qed (p/and-intro <a> <a>)))

(defimplicit releq-refl
  "`(releq-refl R)`
`releq` is reflexive, cf. [[releq-refl-thm]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'releq-refl-thm T U R)))

(defthm releq-sym-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (releq R1 R2)
       (releq R2 R1)))

(proof 'releq-sym-thm
  (assume [H (releq R1 R2)]
    (have <a> _ :by (p/and-intro (p/and-elim-right H)
                                 (p/and-elim-left H))))
  (qed <a>))

(defimplicit releq-sym
  "`(releq-sym R1 R2)`
`releq` is symmetric, cf. [[releq-sym-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'releq-sym-thm T U R1 R2)))

(defthm releq-trans-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)] [R3 (rel T U)]]
  (==> (releq R1 R2)
       (releq R2 R3)
       (releq R1 R3)))

(proof 'releq-trans-thm
  (assume [H1 (releq R1 R2)
           H2 (releq R2 R3)]
    (have <a> (subrel R1 R2) :by (p/and-elim-left H1))
    (have <b> (subrel R2 R3) :by (p/and-elim-left H2))
    (have <c> (subrel R1 R3) :by ((subrel-trans R1 R2 R3) <a> <b>))
    (have <d> (subrel R3 R2) :by (p/and-elim-right H2))
    (have <e> (subrel R2 R1) :by (p/and-elim-right H1))
    (have <f> (subrel R3 R1) :by ((subrel-trans R3 R2 R1) <d> <e>))
    (have <g> _ :by (p/and-intro <c> <f>)))
  (qed <g>))

(defimplicit releq-trans
  "`(releq-trans R1 R2 R3)`
`releq` is transitive, cf. [[releq-trans-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty] [R3 R3-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'releq-trans-thm T U R1 R2 R3)))

(definition rel-equality
  "A *Leibniz*-stype equality for relations.

It says that two relations `R1` and `R2` are equal iff for 
any predicate `P` then `(P R1)` if and only if `(P R2)`.

Note that the identification with [[seteq]] is non-trivial,
 and requires an axiom."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (forall [P (==> (rel T U) :type)]
          (<=> (P R1) (P R2))))

(defimplicit rel-equal
  "`(rel-equal R1 R2) means `R1` and `R2` are equal
in the sense of *Leibniz*, cf. [[rel-equality]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'rel-equality T U R1 R2)))

(defthm rel-equal-prop
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)] [P (==> (rel T U) :type)]]
  (==> (rel-equal R1 R2)
       (P R1)
       (P R2)))

(proof 'rel-equal-prop
  (assume [H (rel-equal R1 R2)
           HR1 (P R1)]
    (have <a> (<=> (P R1) (P R2))
          :by (H P))
    (have <b> (==> (P R1) (P R2))
          :by (p/and-elim-left <a>))
    (have <c> (P R2) :by (<b> HR1)))
  (qed <c>))

(defthm rel-equal-refl-thm
  [[T :type] [U :type] [R (rel T U)]]
  (rel-equal R R))

(proof 'rel-equal-refl-thm
  (assume [P (==> (rel T U) :type)]
    (assume [H1 (P R)]
      (have <a> (P R) :by H1))
    (have <b> _ :by (p/and-intro <a> <a>)))
  (qed <b>))

(defimplicit rel-equal-refl
  "`(rel-equal-refl R)`
`rel-equal` is reflexive, cf. [[rel-equal-refl-thm]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'rel-equal-refl-thm T U R)))

(defthm rel-equal-sym-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (rel-equal R1 R2)
       (rel-equal R2 R1)))

(proof 'rel-equal-sym-thm
  (assume [H (rel-equal R1 R2)]
    (assume [P (==> (rel T U) :type)]
      (assume [H1 (P R2)]
        (have <a> (==> (P R2) (P R1))
              :by (p/and-elim-right (H P)))
        (have <b> (P R1) :by (<a> H1)))
      (assume [H2 (P R1)]
        (have <c> (==> (P R1) (P R2))
              :by (p/and-elim-left (H P)))
        (have <d> (P R2) :by (<c> H2)))
      (have <e> _ :by (p/and-intro <b> <d>))))
  (qed <e>))

(defimplicit rel-equal-sym
  "`(rel-equal-sym R1 R2)`
`rel-equal` is symmetric, cf. [[rel-equal-sym-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'rel-equal-sym-thm T U R1 R2)))

(defthm rel-equal-trans-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)] [R3 (rel T U)]]
  (==> (rel-equal R1 R2)
       (rel-equal R2 R3)
       (rel-equal R1 R3)))

(proof 'rel-equal-trans-thm
  (assume [H1 (rel-equal R1 R2)
           H2 (rel-equal R2 R3)]
    (assume [P (==> (rel T U) :type)]
      (assume [H3 (P R1)]
        (have <a> (==> (P R1) (P R2))
              :by (p/and-elim-left (H1 P)))
        (have <b> (P R2) :by (<a> H3))
        (have <c> (==> (P R2) (P R3))
              :by (p/and-elim-left (H2 P)))
        (have <d> (P R3) :by (<c> <b>)))
      (assume [H4 (P R3)]
        (have <e> (==> (P R3) (P R2))
              :by (p/and-elim-right (H2 P)))
        (have <f> (P R2) :by (<e> H4))
        (have <g> (==> (P R2) (P R1))
              :by (p/and-elim-right (H1 P)))
        (have <h> (P R1) :by (<g> <f>)))
      (have <i> _ :by (p/and-intro <d> <h>))))
  (qed <i>))

(defimplicit rel-equal-trans
  "`(rel-equal-trans R1 R2 R3)`
`rel-equal` is transitive, cf. [[rel-equal-trans-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty] [R3 R3-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'rel-equal-trans-thm T U R1 R2 R3)))

(defthm rel-equal-implies-subrel
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (rel-equal R1 R2)
       (subrel R1 R2)))

(proof 'rel-equal-implies-subrel
  (assume [H (rel-equal R1 R2)
           x T
           y U]
    (pose Qxy := (lambda [R (rel T U)]
                   (R x y)))
    (have <a> (<=> (R1 x y) (R2 x y))
          :by (H Qxy))
    (have <b> (==> (R1 x y) (R2 x y))
          :by (p/and-elim-left <a>)))
  (qed <b>))

(defthm rel-equal-implies-releq
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (rel-equal R1 R2)
       (releq R1 R2)))

(proof 'rel-equal-implies-releq
  (assume [H (rel-equal R1 R2)]
    (have <a> (subrel R1 R2)
          :by ((rel-equal-implies-subrel T U R1 R2) H))
    (have <b> (rel-equal R2 R1)
          :by ((rel-equal-sym R1 R2) H))
    (have <c> (subrel R2 R1)
          :by ((rel-equal-implies-subrel T U R2 R1) <b>))
    (have <d> _ :by (p/and-intro <a> <c>)))
  (qed <d>))

(defaxiom releq-implies-rel-equal-ax
  "As for the set case (cf. [[sets/seteq-implies-set-equal-ax]]),
going from the subset-based equality to the (thus more general) *leibniz*-style
one requires an axiom."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (==> (releq R1 R2)
       (rel-equal R1 R2)))

(defthm rel-equal-releq
  "Coincidence of *Leibniz*-style and subset-based equality for relations."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (<=> (rel-equal R1 R2)
       (releq R1 R2)))

(proof 'rel-equal-releq
  (qed (p/and-intro (rel-equal-implies-releq T U R1 R2)
                    (releq-implies-rel-equal-ax T U R1 R2))))

(definition rcomp-def
  "Sequential relational composition."
  [[T :type] [U :type] [V :type] [R1 (rel T U)] [R2 (rel U V)]]
  (lambda [x T]
    (lambda [z V]
      (exists [y U]
        (and (R1 x y) (R2 y z))))))

(defimplicit rcomp
  "`(rcomp R1 R2)` is the relational composition of `R1` and `R2`, cf. [[rcomp-def]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)
        [_ V] (fetch-rel-type def-env ctx R2-ty)]
    (list #'rcomp-def T U V R1 R2)))

(deflemma rcomp-assoc-subrel-aux
  [[T :type] [U :type] [V :type] [W :type]
   [R1 (rel T U)] [R2 (rel U V)] [R3 (rel V W)] [x T] [z W]]
  (==> ((rcomp R1 (rcomp R2 R3)) x z)
       ((rcomp (rcomp R1 R2) R3) x z)))

(proof 'rcomp-assoc-subrel-aux
  (assume [H ((rcomp R1 (rcomp R2 R3)) x z)]
    (have <a> (exists [y U]
                (and (R1 x y) ((rcomp R2 R3) y z))) :by H)
    (assume [y U
             Hy (and (R1 x y) ((rcomp R2 R3) y z))]
      (have <b> (exists [t V]
                  (and (R2 y t) (R3 t z))) :by (p/and-elim-right Hy))
      (assume [t V
               Ht (and (R2 y t) (R3 t z))]
        (have <c> (and (R1 x y) (R2 y t))
              :by (p/and-intro (p/and-elim-left Hy) (p/and-elim-left Ht)))
        (have <d> ((rcomp R1 R2) x t)
              :by ((q/ex-intro (lambda [k U]
                                       (and (R1 x k) (R2 k t))) y) <c>))
        (have <e> (R3 t z) :by (p/and-elim-right Ht))
        (have <f> _ :by (p/and-intro <d> <e>))
        (have <g> ((rcomp (rcomp R1 R2) R3) x z)
              :by ((q/ex-intro (lambda [k V]
                                       (and ((rcomp R1 R2) x k)
                                            (R3 k z))) t) <f>)))
      (have <h> _ :by ((q/ex-elim  (lambda [k V]
                                           (and (R2 y k) (R3 k z)))
                                   ((rcomp (rcomp R1 R2) R3) x z))
                       <b> <g>)))
    (have <i> _ :by ((q/ex-elim (lambda [k U]
                                        (and (R1 x k) ((rcomp R2 R3) k z)))
                                ((rcomp (rcomp R1 R2) R3) x z))
                     <a> <h>)))
  (qed <i>))

(defthm rcomp-assoc-subrel
  [[T :type] [U :type] [V :type] [W :type]
   [R1 (rel T U)] [R2 (rel U V)] [R3 (rel V W)]]
  (subrel (rcomp R1 (rcomp R2 R3))
          (rcomp (rcomp R1 R2) R3)))

(proof 'rcomp-assoc-subrel
  (assume [x T
           y W
           H ((rcomp R1 (rcomp R2 R3)) x y)]
    (have <a> ((rcomp (rcomp R1 R2) R3) x y)
          :by ((rcomp-assoc-subrel-aux
                T U V W R1 R2 R3)
               x y H)))
  (qed <a>))

(deflemma rcomp-assoc-suprel-aux
  [[T :type] [U :type] [V :type] [W :type]
   [R1 (rel T U)] [R2 (rel U V)] [R3 (rel V W)] [x T] [z W]]
  (==> ((rcomp (rcomp R1 R2) R3) x z)
       ((rcomp R1 (rcomp R2 R3)) x z)))

(proof 'rcomp-assoc-suprel-aux
  (assume [H ((rcomp (rcomp R1 R2) R3) x z)]
    (have <a> (exists [y V]
                (and ((rcomp R1 R2) x y)
                     (R3 y z))) :by H)
    (assume [y V
             Hy (and ((rcomp R1 R2) x y)
                     (R3 y z))]
      (have <b> (exists [t U] (and (R1 x t) (R2 t y)))
            :by (p/and-elim-left Hy))
      (assume [t U
               Ht (and (R1 x t) (R2 t y))]
        (have <c1> (R1 x t) :by (p/and-elim-left Ht))
        (have <c2> (and (R2 t y) (R3 y z))
              :by (p/and-intro (p/and-elim-right Ht)
                               (p/and-elim-right Hy)))
        (have <c3> ((rcomp R2 R3) t z)
              :by ((q/ex-intro (lambda [k V]
                                       (and (R2 t k) (R3 k z))) y)
                   <c2>))
        (have <c> ((rcomp R1 (rcomp R2 R3)) x z)
              :by ((q/ex-intro (lambda [k U]
                                       (and (R1 x k)
                                            ((rcomp R2 R3) k z))) t)
                   (p/and-intro <c1> <c3>))))
      (have <d> _ :by ((q/ex-elim (lambda [t U] (and (R1 x t) (R2 t y)))
                                  ((rcomp R1 (rcomp R2 R3)) x z))
                       <b> <c>)))
    (have <e> _ :by ((q/ex-elim (lambda [y V]
                                        (and ((rcomp R1 R2) x y)
                                             (R3 y z)))
                                ((rcomp R1 (rcomp R2 R3)) x z))
                     <a> <d>)))
  (qed <e>))

(defthm rcomp-assoc-suprel
  [[T :type] [U :type] [V :type] [W :type]
   [R1 (rel T U)] [R2 (rel U V)] [R3 (rel V W)]]
  (subrel (rcomp (rcomp R1 R2) R3)
          (rcomp R1 (rcomp R2 R3))))

(proof 'rcomp-assoc-suprel
  (assume [x T
           z W]
    (have <a> _ :by (rcomp-assoc-suprel-aux
                     T U V W
                     R1 R2 R3 x z)))
  (qed <a>))

(defthm rcomp-assoc-thm
  "Relational composition is associative."
  [[T :type] [U :type] [V :type] [W :type]
   [R1 (rel T U)] [R2 (rel U V)] [R3 (rel V W)]]
  (releq (rcomp R1 (rcomp R2 R3))
         (rcomp (rcomp R1 R2) R3)))

(proof 'rcomp-assoc-thm
  (qed (p/and-intro (rcomp-assoc-subrel T U V W R1 R2 R3)
                    (rcomp-assoc-suprel T U V W R1 R2 R3))))

(defimplicit rcomp-assoc
  "`(rcomp-assoc R1 R2 R3)`. Relational composition is associative, cf. [[recomp-assoc-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty] [R3 R3-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)
        [_ V] (fetch-rel-type def-env ctx R2-ty)
        [_ W] (fetch-rel-type def-env ctx R3-ty)]
    (list #'rcomp-assoc-thm T U V W R1 R2 R3)))

(definition psubrel-def
  "The anti-reflexive variant of [[subrel]]."
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (and (subrel R1 R2)
       (not (releq R1 R2))))

(defimplicit psubrel
  "`(psubrel R1 R2)` means `R1` si a sub-relation of `R2`, cf. [[psubrel-def]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'psubrel-def T U R1 R2)))

(defthm psubrel-antirefl-thm
  [[T :type] [U :type] [R (rel T U)]]
  (not (psubrel R R)))

(proof 'psubrel-antirefl-thm
  (assume [H (psubrel R R)]
    (have <a> (not (releq R R))
          :by (p/and-elim-right H))
    (have <b> p/absurd :by (<a> (releq-refl R))))
  (qed <b>))

(defimplicit psubrel-antirefl
  "`(psubrel-antirefl R)` means proper sub-relation is antireflexive, cf. [[psubrel-antirefl-thm]]."
  [def-env ctx [R R-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R-ty)]
    (list #'psubrel-antirefl-thm T U R)))

(defthm psubrel-antisym-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)]]
  (not (and (psubrel R1 R2)
            (psubrel R2 R1))))

(proof 'psubrel-antisym-thm
  (assume [H (and (psubrel R1 R2)
                  (psubrel R2 R1))]
    (have <a> (not (releq R1 R2))
          :by (p/and-elim-right (p/and-elim-left H)))
    (have <b> (releq R1 R2)
          :by (p/and-intro (p/and-elim-left (p/and-elim-left H))
                           (p/and-elim-left (p/and-elim-right H))))
    (have <c> p/absurd :by (<a> <b>)))
  (qed <c>))

(defimplicit psubrel-antisym
  "`(psubrel-antisym R1 R2)` means proper sub-relation is antisymmetric, 
cf. [[psubrem-antisym-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'psubrel-antisym-thm T U R1 R2)))

(defthm psubrel-trans-thm
  [[T :type] [U :type] [R1 (rel T U)] [R2 (rel T U)] [R3 (rel T U)]]
  (==> (psubrel R1 R2)
       (psubrel R2 R3)
       (psubrel R1 R3)))

(proof 'psubrel-trans-thm
  (assume [H1 (psubrel R1 R2)
           H2 (psubrel R2 R3)]
    (have <a> (subrel R1 R3)
          :by ((subrel-trans R1 R2 R3)
               (p/and-elim-left H1)
               (p/and-elim-left H2)))
    (assume [H3 (releq R1 R3)]
      (have <b> (rel-equal R1 R3)
            :by ((releq-implies-rel-equal-ax T U R1 R3)
                 H3))
      (have <c> (psubrel R3 R2)
            :by ((rel-equal-prop T U R1 R3 (lambda [R (rel T U)]
                                                   (psubrel R R2)))
                 <b> H1))
      (have <d> p/absurd :by ((psubrel-antisym R2 R3)
                              (p/and-intro H2 <c>))))
    (have <e> _ :by (p/and-intro <a> <d>)))
  (qed <e>))

(defimplicit psubrel-trans
  "`(psubrel-trans R1 R2 R3)` means proper sub-relation is transitive, cf. [[psubrel-trans-thm]]."
  [def-env ctx [R1 R1-ty] [R2 R2-ty] [R3 R3-ty]]
  (let [[T U] (fetch-rel-type def-env ctx R1-ty)]
    (list #'psubrel-trans-thm T U R1 R2 R3)))

(defthm psubrel-emptyrel
  [[T :type] [U :type] [R (rel T U)]]
  (==> (psubrel (emptyrel T U) R)
       (not (releq R (emptyrel T U)))))

(proof 'psubrel-emptyrel
  (assume [H (psubrel (emptyrel T U) R)]
    (assume [H' (releq R (emptyrel T U))]
      (have <a> (not (releq (emptyrel T U) R))
            :by (p/and-elim-right H))
      (have <b> (releq  (emptyrel T U) R)
            :by ((releq-sym R (emptyrel T U)) H'))
      (have <c> p/absurd :by (<a> <b>))))
  (qed <c>))


(defthm psubrel-emptyrel-conv
  [[T :type] [U :type] [R (rel T U)]]
  (==> (not (releq R (emptyrel T U)))
       (psubrel (emptyrel T U) R)))

(proof 'psubrel-emptyrel-conv
  (assume [H (not (releq R (emptyrel T U)))]
    (have <a> (subrel (emptyrel T U) R)
          :by (subrel-emptyrel-lower-bound T U R))
    (assume [H' (releq (emptyrel T U) R)]
      (have <b> (releq R (emptyrel T U))
            :by ((releq-sym (emptyrel T U) R) H'))
      (have <c> p/absurd :by (H <b>)))
    (have <d> (psubrel (emptyrel T U) R)
          :by (p/and-intro <a> <c>)))
  (qed <d>))

(defthm psubrel-emptyrel-equiv
  [[T :type] [U :type] [R (rel T U)]]
  (<=> (psubrel (emptyrel T U) R)
       (not (releq R (emptyrel T U)))))

(proof 'psubrel-emptyrel-equiv
  (qed (p/and-intro (psubrel-emptyrel T U R)
                    (psubrel-emptyrel-conv T U R))))

(definition prod-def
  "The cartersian product of sets `s1` and `s2`, i.e. `s1`⨯`s2`."
  [[T :type] [U :type] [s1 (set T)] [s2 (set U)]]
  (lambda [x T]
    (lambda [y U]
      (and (elem x s1)
           (elem y s2)))))

(defimplicit prod
  "`(prod s1 s2)` is the cartersian product of `s1` and `s2` i.e. `s1`⨯`s2`,
cf. [[prod-def]]."
  [def-env ctx [s1 s1-ty] [s2 s2-ty]]
  (let [T (sets/fetch-set-type def-env ctx s1-ty)
        U (sets/fetch-set-type def-env ctx s2-ty)]
    (list #'prod-def T U s1 s2)))




