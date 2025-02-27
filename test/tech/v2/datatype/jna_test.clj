(ns tech.v2.datatype.jna-test
  (:require [clojure.test :refer :all]
            [tech.v2.datatype :as dtype]
            [tech.v2.datatype.protocols :as dtype-proto]
            [tech.v2.datatype.base :as base]
            [tech.v2.datatype.jna :as dtype-jna]
            [tech.jna :as jna]
            [tech.v2.datatype.typed-buffer :as typed-buffer]
            [tech.parallel.for :as parallel-for]))


(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)


(deftest jna-things-are-typed-pointers
  (let [test-buf (dtype-jna/make-typed-pointer :uint8 (range 255 245 -1))]
    (is (= :uint8 (dtype-proto/get-datatype test-buf)))
    (is (typed-buffer/typed-buffer? test-buf))
    (is (identical? test-buf (typed-buffer/->typed-buffer test-buf)))
    (let [data-buf (int-array (dtype/ecount test-buf))]
      (dtype/copy! test-buf data-buf)
      (is (= (vec (range 255 245 -1))
             (dtype/->vector data-buf))))))


(deftest copy-time-test
  (testing "Run perf regression of times spent to copy data"
    (let [num-items (long 100000)
          src-data (float-array (range num-items))
          dst-data (float-array num-items)
          array-copy (fn []
                       (parallel-for/parallel-for
                        idx num-items
                        (aset dst-data idx (aget src-data idx))))

          src-ptr (dtype-jna/make-typed-pointer :float32 src-data)
          dst-ptr (dtype-jna/make-typed-pointer :float32 num-items)

          ptr-ptr-copy (fn []
                         (dtype/copy! src-ptr 0 dst-ptr 0 num-items))

          array-ptr-copy (fn []
                           (dtype/copy! src-data 0 dst-ptr 0 num-items))
          fns {:array-hand-copy array-copy
               :ptr-ptr-copy ptr-ptr-copy
               :array-ptr-copy array-ptr-copy}
          run-timed-fns (fn []
                          (->> fns
                               (map (fn [[fn-name time-fn]]
                                      [fn-name (with-out-str
                                                 (time
                                                  (dotimes [iter 400]
                                                    (time-fn))))]))
                               (into {})))
          warmup (run-timed-fns)
          times (run-timed-fns)]
      (println times))))


(deftest set-constant!
  (let [test-buf (dtype-jna/make-typed-pointer :int64 5)]
    (dtype/set-constant! test-buf 0 1 (dtype/ecount test-buf))
    (is (= [1 1 1 1 1] (dtype/->vector test-buf)))
    (is (= [1 1 1 1 1] (-> (dtype/clone test-buf :datatype :uint8)
                           dtype/->vector)))
    (is (not= 0 (-> (dtype/clone test-buf :datatype :uint8)
                    jna/->ptr-backing-store
                    dtype-jna/pointer->address)))))


(deftest simple-init-ptr
  (let [test-ptr (dtype-jna/make-typed-pointer :int64 [2 2])]
    (is (= [2 2]
           (dtype/->vector test-ptr))))
  (let [test-ptr (dtype-jna/make-typed-pointer :float64 [2 2])]
    (is (= [2.0 2.0]
           (dtype/->vector test-ptr)))))
