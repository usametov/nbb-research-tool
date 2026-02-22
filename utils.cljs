(ns utils
  (:require [clojure.string :as str]))

;; Track subagent names by their tool_use_id
(def subagent-registry (atom {}))

(defn truncate
  "Truncate a value for display."
  [value max-length]
  (let [text (str value)]
    (if (> (count text) max-length)
      (str (subs text 0 max-length) "...")
      text)))

(defn format-input
  "Format tool input for readable display."
  ([input-dict] (format-input input-dict 200))
  ([input-dict max-length]
   (if (or (nil? input-dict) (empty? input-dict))
     "{}"
     (let [parts (for [[key value] (js/Object.entries input-dict)]
                   (let [val-str (str value)
                         trimmed (if (> (count val-str) 50)
                                   (str (subs val-str 0 50) "...")
                                   val-str)]
                     (str key "=" trimmed)))]
       (truncate (str/join ", " parts) max-length)))))

(defn get-agent-label
  "Determine source agent label with color codes."
  [message]
  (let [parent-id (.-parent_tool_use_id message)]
    (if parent-id
      (let [subagent-name (get @subagent-registry parent-id "unknown")]
        (str "\u001b[35m[Subagent " subagent-name "]\u001b[0m"))
      "\u001b[36m[Main]\u001b[0m")))

(defn display-message
  "Display an AssistantMessage with color-coded agent labels."
  [message]
  (let [agent-label (get-agent-label message)]
    (doseq [block (.-content message)]
      (let [block-type (.-type block)]
        (cond
          (= block-type "tool_use")
          (let [tool-name (.-name block)]
            (if (= tool-name "Task")
              (let [subagent-type (or (some-> block .-input .-subagent_type) "unknown")
                    description (or (some-> block .-input .-description) "")
                    tool-id (.-id block)]
                (when tool-id
                  (swap! subagent-registry assoc tool-id subagent-type))
                (println (str agent-label " 🚀 Spawning subagent: \u001b[1m" subagent-type "\u001b[0m"))
                (when (not (str/blank? description))
                  (println (str "   Description: " description))))
              (let [tool-id (if-let [id (.-id block)]
                              (subs id 0 8)
                              "unknown")]
                (println (str agent-label " 🔧 \u001b[1m" tool-name "\u001b[0m (id: " tool-id ")"))
                (println (str "   Input: " (format-input (.-input block)))))))
          (= block-type "text")
          (println (str "\u001b[1mClaude\u001b[0m: " (.-text block) "\n")))))))