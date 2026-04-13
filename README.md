# Temporal D&D Encounter Generator

A demo showcasing [Temporal](https://temporal.io) for building resilient, retry-capable distributed workflows. This project builds a D&D 5e encounter generator with the saga pattern, demonstrating how Temporal handles failures and compensating transactions.

## Overview

This project demonstrates key Temporal features:

- **Resilient Workflows**: Activities automatically retry with exponential backoff when external services fail
- **Saga Pattern**: Token-based accounting with automatic compensation on failure
- **State Preservation**: Temporal maintains workflow state across failures and service restarts
- **Deterministic Execution**: Replays through event history ensure consistent results

## Prerequisites

- **Java 25** (or later)
- **Maven 3.8+**
- **Docker** and **Docker Compose**

## Quick Start

### 1. Clone and Build

```bash
git clone <repo-url>
cd temporal_prep
mvn clean install
```

### 2. Start Infrastructure

```bash
docker-compose up -d
```

This starts:
- **Temporal Server** on http://localhost:7233 (gRPC)
- **Temporal UI** on http://localhost:8080
- **PostgreSQL 15** (Temporal database backend)
- **Ollama 7B Mistral** on http://localhost:11434 (LLM)

Wait ~10 seconds for services to stabilize.

### 3. Run the Workflow

```bash
./run.sh
```

Or with custom parameters:

```bash
./run.sh 6 8    # 6 characters, level 8
```

Example output:
```
╔════════════════════════════════════════════════════════════╗
║                    ENCOUNTER GENERATED                     ║
╚════════════════════════════════════════════════════════════╝

📋 The party stumbles upon a goblin ambush in the forest.
   A group of goblins and their orc captain leap from the
   underbrush, weapons drawn.

⚔️  Combat Threats:
  1. Orc Captain           (CR 2.0) | 450 XP | Humanoid (Medium)
     └─ A brutal orc warrior hardened by countless battles.
  2. Goblin                (CR 0.3) |  50 XP | Humanoid (Small)
     └─ A cunning little menace with a rusty blade.
  3. Goblin                (CR 0.3) |  50 XP | Humanoid (Small)
     └─ A cunning little menace with a rusty blade.

💰 Challenge Summary:
   • Total XP Budget: 1,000
   • Encounter XP: 550
   • Creature Count: 3
```

View the workflow execution in real-time at http://localhost:8080.

## Demo Scenarios

### Scenario 1: Success (Happy Path)

```bash
./run.sh      # Uses defaults: 4 characters, level 5
```

**Observe:**
- Tokens deducted from `data/token_ledger.json` (1000 → 950)
- Temporal UI shows all activities completing successfully
- Encounter details printed to console

### Scenario 2: Recovery from Failure

While the workflow is running:

```bash
# In another terminal, stop Ollama (simulates external service failure)
./stop-ollama.sh
```

**Observe in Temporal UI:**
- GenerateDescriptionActivity fails
- Workflow automatically retries with backoff (10s → 20s → 40s → 60s)
- Console shows retry attempts

Then restore the service:

```bash
./restart-ollama.sh
```

**Observe:**
- Workflow resumes from the failed activity
- Completes successfully without restarting from the beginning
- Tokens remain deducted (950) because workflow eventually succeeded

**Key insight**: Temporal preserves state across failures. When Ollama comes back online, the activity resumes execution with the exact same inputs.

### Scenario 3: Complete Failure with Rollback

While the workflow is running:

```bash
# In another terminal, stop Ollama (and don't restart it)
./stop-ollama.sh
```

**Observe:**
- GenerateDescriptionActivity fails and retries (10s → 20s → 40s → 60s)
- After max retries exhausted, workflow fails entirely
- RefundTokensActivity is automatically triggered
- Check token ledger: tokens refunded (950 → 1000)
- Token ledger file shows DEDUCT and REFUND entries

**Key insight**: Temporal's saga pattern ensures compensation happens automatically. Even after the workflow fails, the refund activity runs to roll back the token deduction.

## Testing

Run all tests:

```bash
mvn test
```

## Troubleshooting

### Services won't start

```bash
# Check Docker is running
docker ps

# View logs
docker-compose logs temporal
docker-compose logs postgres
docker-compose logs ollama
```

### Workflow times out

Wait for Ollama to stabilize (first inference is slow).

### Token ledger not updating

Ensure `data/` directory exists:

```bash
mkdir -p data
```
