# nbb-research-tool
A multi-agent system built with the Claude Agent SDK that includes documentation research, repository analysis, and web research capabilities.

The system has been converted to run on **nbb (Node.js Babashka)** using ClojureScript with the Claude TypeScript SDK. The Python version is available here: https://github.com/https-deeplearning-ai/sc-agent-skills-files/blob/main/L7


### Setup

1. Install Node.js dependencies:
   ```bash
   npm install
   ```

2. Ensure nbb is available (installed automatically as dev dependency). Alternatively install globally: `npm install -g nbb`.

3. The same `.env` file is used (make sure it exists).

### Running the ClojureScript Agent

```bash
npx nbb agent.cljs
```

Or using the npm script:
```bash
npm start
```

Type your messages and press Enter. Type `exit` to quit.

### Project Structure

- `agent.cljs` - Main entry point (replaces agent.py)
- `utils.cljs` - Utility functions (replaces utils.py)
- `prompts/` - Prompt files (unchanged)
- `package.json` - npm dependencies
- `deps.edn` - Clojure/nbb dependencies

### Notes

- The conversion uses the `@anthropic-ai/claude-agent-sdk` TypeScript SDK. The API is similar to the Python SDK but uses camelCase option keys (e.g., `systemPrompt`, `settingSources`, `mcpServers`).
- Environment variables are loaded via `dotenv` and passed to the SDK via the `env` option.
- MCP server configuration is identical to Python version.
- The interactive loop uses Node.js `readline` for simplicity.

### Troubleshooting

If the agent hangs or fails to respond, check:
- API key and base URL are correctly set in `.env`
- The Claude Code CLI (`claude`) is installed globally (`npm install -g @anthropic-ai/claude-code`)
- The SDK may require additional configuration (see SDK documentation)

### Development

To modify the ClojureScript code, you can edit `.cljs` files and run with nbb. The `promesa` library is used for promise handling.

### Next Steps for Full Testing

  To fully test the system:

  1. Set up Claude Code CLI: Ensure claude command is available globally
  2. Test API connectivity: Verify DeepSeek API key works with TypeScript SDK
  3. Run integration tests: Execute npx nbb agent.cljs and test basic queries
  4. Verify subagent spawning: Test that the Task tool properly spawns subagents
  5. Check MCP server integration: Ensure Notion MCP server starts correctly

  The implementation follows the plan's architecture while adapting Python idioms to
  ClojureScript/JavaScript patterns. The system maintains all original functionality: multi-agent
  orchestration, tool delegation, prompt loading, and interactive CLI interface.


