(ns immutant.lein.util
  (:require [leiningen.core.main :as lcm]
            [leiningen.core.project :as project]
            [clojure.tools.cli :as opts]
            [clojure.string :as str]))

(defn options-summary [option-specs]
  (:summary (opts/parse-opts [] option-specs)))

(defn parse-options [args option-specs help-fn]
  (let [{:keys [options errors]}
        (opts/parse-opts args option-specs)
        abort #(lcm/abort (format "%s\n\n%s" % (help-fn)))]
    (when errors
      (abort (str/join "\n" errors)))
    (reduce
      (fn [accum [k v]]
        (if (.startsWith (name k) "no-")
          (let [other (keyword (.substring (name k) 3))]
            (when (contains? options other)
              (abort (apply format "You can't specify both --%s and --%s"
                       (map #(-> % name (.replace "?" "")) [k other]))))
            (-> accum
              (dissoc k)
              (assoc other false)))
          (assoc accum k v))) {} options)))

(defn mapply [f & args]
  "Applies args to f, and expands the last arg into a kwarg seq if it is a map"
  (apply f (apply concat (butlast args) (last args))))

(defn extract-profiles [project]
  (when-let [profiles (seq (-> project meta :included-profiles))]
    (when-not (= profiles
                (:default @project/default-profiles))
      (vec profiles))))
