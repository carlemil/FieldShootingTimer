# Plan: Interactive Feature Planning with Task Generation

Plan a new feature through interactive discussion with the user, then generate task files for TDD implementation.

Usage: `/plan`

Perform the following steps in order.

---

## Step 1 — Ask what to build

Ask the user: **"What feature do you want to plan?"**

Wait for their response before continuing.

---

## Step 2 — Explore the codebase

Based on the user's described feature, explore the relevant parts of the codebase to understand:
- Which existing files, classes, and patterns are involved
- How similar features are currently implemented
- What data layer, DI, and UI components already exist that can be reused
- Any constraints or conventions from CLAUDE.md that apply

Summarize your findings to the user in 3-5 bullet points.

---

## Step 3 — Ask clarifying questions

Based on your codebase exploration and the user's feature description, identify any uncertainties or ambiguities. Ask the user **up to 5 focused questions** covering:

- **Scope:** What exactly should be included vs excluded?
- **Behavior:** How should edge cases work? (empty states, errors, loading)
- **Data:** Does the API already support this, or is it client-side only?
- **UI:** Any specific design or interaction requirements?
- **Priority:** Which parts are most important if the feature needs to be split?

Ask all questions in a single numbered list. Wait for the user's answers before continuing.

---

## Step 4 — Follow up if needed

If the user's answers reveal new uncertainties, ask a short follow-up round (max 3 questions). If everything is clear, skip to Step 5.

---

## Step 5 — Present the plan

Present a concise plan to the user with:
- **Overview:** 1-2 sentence summary of the feature
- **Tasks:** A numbered list of discrete implementation tasks, each with:
  - A short title
  - Category (e.g., Feature, UI, Data, DI)
  - Priority (LOW / MEDIUM / HIGH)
  - Which files will be created or modified
  - What the task involves (1-2 sentences)
- **Order:** The recommended implementation order

Ask the user: **"Does this plan look good? Any tasks to add, remove, or change?"**

Wait for confirmation or adjustments.

---

## Step 6 — Generate task files

For each task in the confirmed plan, create a markdown file under `tasks/` following this exact format:

```
tasks/<category-lowercase>/<category-lowercase>-<short-kebab-name>.md
```

Each file must follow this structure:

```markdown
# <PRIORITY> — <Short Title>

**Category:** <Category>
**Priority:** <PRIORITY>
**Status:** TODO

## Files
- `<relative-path-from-app/src/main/java/se/kjellstrand/webshooter/>:<line-number-if-known>`

## Issue
<What needs to be built or changed, stated as the current gap or problem.>

## Fix
<What specifically to implement. Be precise: name the classes, methods, patterns to use.>
```

Guidelines for task files:
- Paths in `## Files` are relative to `app/src/main/java/se/kjellstrand/webshooter/`
- For new files, write the expected path with `(new)` suffix
- Each task should be independently implementable and testable
- Order tasks so dependencies come first
- Keep each task focused — one concern per file

---

## Step 7 — List created tasks

Show the user a summary of all created task files:

```
Created N task files:
  tasks/<category>/<name>.md — <title>
  ...
```

---

## Step 8 — Run TDD

For each task file created (in order), invoke the `/tdd` skill:

```
/tdd <category>/<name-without-extension>
```

Process tasks one at a time, in the recommended implementation order from Step 5.
