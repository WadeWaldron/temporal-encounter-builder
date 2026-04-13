#!/bin/bash

# Default values
CHARACTERS=${1:-4}
LEVEL=${2:-5}

# Run the Temporal workflow
mvn clean compile exec:java -Dexec.args="-c $CHARACTERS -l $LEVEL"