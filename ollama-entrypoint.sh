#!/bin/bash
set -e

# Start Ollama in the background
ollama serve &
OLLAMA_PID=$!

# Wait for Ollama and pull Mistral model with output
echo "Waiting for Ollama..."
for i in {1..30}; do
  if ollama pull mistral 2>&1; then
    echo ""
    echo "✓ Mistral model ready!"
    break
  fi
  echo "Attempt $i/30: Waiting for Ollama..."
  sleep 1
done

# Wait for the background process
wait $OLLAMA_PID
