# ARGOS-1

ARGOS-1 (Architectural Review & Governance Orchestration System) is a sovereign, voice-activated AI Agent designed to serve as an "Automated Lead Engineer." It bridges the gap between static code analysis and human-level architectural reasoning.

In Greek mythology, Argos Panoptes was the "All-Seeing" watchman with one hundred eyes. Like its namesake, ARGOS-1 monitors the codebase from every angle—security, structural integrity, and logic—ensuring that no "architectural drift" occurs during high-velocity development.

## Env variables

- For the wake word ("Argos one") to work with dictation, you will need a Picovoice access key. Add it to VITE_PICOVOICE_ACCESS_KEY in the frontend .env.
- For voice dictation to work, you will need an Elevenlabs API key. Place it in the VITE_ELEVENLABS_API_KEY frontend/.env file.
  ☝️ The app works fine without these extra features. You just need to manaully type, hit execute, and read. Nothing we're not already accustomed to doing in the 21st century.

## Setup

- Install JAVA 21
- Install Ollama. Download deepseek-r1:8b.
- In terminal 1: `make run-backend`
- In terminal 2: `make run-frontend`
