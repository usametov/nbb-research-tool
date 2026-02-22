(ns agent
  (:require ["@anthropic-ai/claude-agent-sdk" :refer [query]]
            ["dotenv" :refer [config]]
            ["fs" :as fs]
            ["readline" :as rl]
            ["process" :as process]
            [promesa.core :as p]
            [utils :refer [display-message]])
  (:require-macros [promesa.core :as p]))

;; Load .env file
(config)

;; Environment variables
(def notion-token (.-NOTION_TOKEN js/process.env))
(def api-key (.-ANTHROPIC_API_KEY js/process.env))
(def base-url (.-ANTHROPIC_BASE_URL js/process.env))

;; Constants
(def prompts-dir "prompts")

;; Helper to load prompt files
(defn load-prompt [filename]
  (-> (fs/readFileSync (str prompts-dir "/" filename) "utf8")
      (.trim)))

;; Process query async generator
(defn process-query [q]
  (p/loop []
    (p/let [result (.next q)]
      (when (not (.-done result))
        (let [message (.-value result)]
          (when (= (.-type message) "assistant")
            (display-message message))
          (recur))))))

;; Main function
(defn -main []
  (println "Starting conversation session.")
  (println "Type 'exit' to quit\n")
  ;; Load prompts
  (let [main-agent-prompt (load-prompt "main_agent.md")
        docs-researcher-prompt (load-prompt "docs_researcher.md")
        repo-analyzer-prompt (load-prompt "repo_analyzer.md")
        web-researcher-prompt (load-prompt "web_researcher.md")
        agents #js {"docs_researcher" #js {:description "Finds and extracts information from official documentation sources."
                                           :prompt docs-researcher-prompt
                                           :tools #js ["WebSearch" "WebFetch"]
                                           :model "haiku"}
                    "repo_analyzer" #js {:description "Analyzes code repositories for structure, examples, and implementation details."
                                         :prompt repo-analyzer-prompt
                                         :tools #js ["WebSearch" "Bash"]
                                         :model "haiku"}
                    "web_researcher" #js {:description "Finds articles, videos, and community content."
                                          :prompt web-researcher-prompt
                                          :tools #js ["WebSearch" "WebFetch"]
                                          :model "haiku"}}
        options #js {:systemPrompt main-agent-prompt
                     :settingSources #js ["user" "project"]
                     :mcpServers #js {"notion" #js {:command "npx"
                                                    :args #js ["-y" "@notionhq/notion-mcp-server"]
                                                    :env #js {:NOTION_TOKEN notion-token}}}
                     :allowedTools #js ["Skill" "Task" "Write" "Bash" "WebSearch" "WebFetch"
                                        "mcp__notion__API-post-search" "mcp__notion__API-patch-block-children"]
                     :model "sonnet"
                     :agents agents
                     :env #js {:ANTHROPIC_API_KEY api-key
                               :ANTHROPIC_BASE_URL base-url
                               :NOTION_TOKEN notion-token}}]
    (println "Agents loaded. Ready for input.")
    ;; Interactive loop
    (let [interface (rl/createInterface #js {:input (.-stdin js/process)
                                             :output (.-stdout js/process)})]
      (p/loop []
        (p/let [user-input (p/promise (fn [resolve]
                                        (.question interface "You: " resolve)))]
          (if (= (clojure.string/lower-case user-input) "exit")
            (do
              (.close interface)
              (println "Goodbye!"))
            (p/let [q (query #js {:prompt user-input :options options})]
              (process-query q)
              (recur))))))))

;; Entry point for nbb
(when (= js/process.argv[1] js/__filename)
  (-main))