# Ecosystem Direction

TimeTask Pro V2 is designed to stand on its own as an open-source, local-first Android productivity app. Its core responsibility is time management: tasks, schedules, notes, calendars, timers, reminders, templates, and productivity review flows.

The long-term product direction is broader than a standalone task manager. TimeTask Pro V2 is intended to become the planning and time-control layer of a local-first AI productivity ecosystem.

## Companion Direction

RefrAIct AI Android is being developed separately as a personal AI workspace focused on model interaction, local history, memory-oriented workflows, agent sessions, and multi-model reasoning.

TimeTask Pro V2 and RefrAIct AI Android are separate projects today:

- RefrAIct source code is not included in this repository.
- TimeTask Pro V2 has no runtime dependency on RefrAIct.
- There is no hidden data sync between the two projects.
- Any future integration must be optional and user-controlled.

## Why They Fit Together

TimeTask Pro V2 answers planning questions:

- What should I do?
- When should I do it?
- What is urgent or important?
- How much time is available?
- What did I complete this week?

RefrAIct AI Android is intended to answer thinking and context questions:

- What does my broader work context mean?
- Which model or agent should help with this task?
- What did previous AI sessions decide?
- Which memories, pinned facts, or long-running runs matter now?
- How can several models reason over the same problem?

Together, the future direction is a personal productivity system where time planning and AI reasoning can cooperate without forcing private user data into uncontrolled remote workflows.

## Future Integration Principles

Any future companion integration should follow these rules:

- Optional by default: TimeTask must remain useful without RefrAIct.
- Local-first: task data should remain on device unless the user explicitly shares it.
- User-approved context: users should review what task or schedule context is sent.
- Minimal context: share only the data needed for the selected action.
- Review before write: AI output should become a draft or suggestion before it changes local data.
- No hidden background sharing: integrations must not silently upload task history.
- Clear boundaries: TimeTask owns planning data; RefrAIct owns AI workspace and reasoning sessions.

## Possible Future Work

Potential future integration contracts:

- Export selected TimeTask tasks into a RefrAIct reasoning session.
- Send a user-approved daily plan draft to RefrAIct for deeper analysis.
- Import an AI-generated task draft back into TimeTask after user review.
- Link a TimeTask task to a RefrAIct chat or run ID.
- Use local Android intents or a documented local contract before considering any networked integration.

These are roadmap ideas, not production features in the current alpha release.

## Current Status

The current repository contains:

- TimeTask Pro V2 Android app foundation.
- OpenAI task parsing architecture.
- Structured Outputs schema for AI task drafts.
- Local fallback parsing.
- Privacy-first documentation.

It does not contain:

- RefrAIct source code.
- A production RefrAIct integration.
- Cross-app synchronization.
- Automatic AI writes into the task database.
