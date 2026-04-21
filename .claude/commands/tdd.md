# TDD: Fix a Task with Test-Driven Development

Fix a task from the `tasks/` directory using a strict red-green TDD cycle. The argument is the path to the task file relative to `tasks/` without the `.md` extension.

Usage: `/tdd bug/bug-unsafe-non-null-assertion`

Perform the following steps in order. Stop and report if any step fails or an expectation is not met (after retry).

---

## Step 0 — Clear context

Before doing anything else, save the value of `$ARGUMENTS` (the task file path) so it is not lost. Then run the `/clear` command to reset the conversation context and start with a clean slate. After clearing, continue with Step 1 using the saved argument value.

---

## Step 1 — Read and parse the task file

Read the file `tasks/$ARGUMENTS.md`.

If the file does not exist, stop and tell the user: "Task file not found: tasks/$ARGUMENTS.md". List the available task categories by listing the `tasks/` directory.

Parse the following fields from the task file:
- **Title** (the H1 heading, e.g., "MEDIUM — Unsafe Non-Null Assertion")
- **Category** (from the `**Category:**` line)
- **Priority** (from the `**Priority:**` line)
- **Files** (the list under `## Files` — each entry has a path and optional line numbers)
- **Issue** (the text under `## Issue`)
- **Fix** (the text under `## Fix`)

Show the user a summary of the task before continuing.

---

## Step 2 — Read the affected source files

For each file path listed in the `## Files` section:
- The paths are relative to `app/src/main/java/se/kjellstrand/webshooter/`. Prepend that prefix to get the full path.
- Strip any line number suffix (`:129` or `:30-35`) from the path for reading.
- Read the full file, paying special attention to the referenced line numbers.
- Understand the surrounding context: the class, its imports, its dependencies, and how the problematic code is used.

If a referenced file does not exist at the expected path, try searching for it with a glob pattern. If still not found, stop and tell the user.

---

## Step 3 — Write tests (red + guard)

### 3a. Determine test file location

The test file should mirror the source file's package structure under `app/src/test/java/se/kjellstrand/webshooter/`. For example:
- Source: `ui/screens/signup/SignupScreen.kt` → Test: `app/src/test/java/se/kjellstrand/webshooter/ui/screens/signup/SignupScreenTest.kt`
- Source: `data/AuthCookieJar.kt` → Test: `app/src/test/java/se/kjellstrand/webshooter/data/AuthCookieJarTest.kt`

If the test file already exists, add tests to it rather than overwriting it.

### 3b. Write the test class

Follow these project conventions:
- **JUnit 4** with `@RunWith(RobolectricTestRunner::class)` (only if Android framework classes are needed; use plain JUnit 4 if not)
- **Backtick method names** describing the behavior, e.g., `` `error property is handled safely when null` ``
- **Standard imports**: `org.junit.Test`, `org.junit.Assert.*`, `org.junit.runner.RunWith`, `org.robolectric.RobolectricTestRunner`
- Match the package declaration to the directory structure.

Write two groups of tests, clearly separated by comments:

```kotlin
// --- Fixed behavior (should FAIL before fix, PASS after fix) ---
```

These tests assert the NEW correct behavior described in the task's `## Fix` section. On the current unfixed code, they must fail.

```kotlin
// --- Guard tests (should PASS before and after fix) ---
```

These tests verify existing correct behavior that must not regress. They exercise the same code path but for cases that already work correctly.

Aim for 2-5 tests in each group, depending on the complexity of the issue.

### 3c. Compile check

Run:
```
./gradlew :app:compileProdReleaseSources --no-daemon
```

If compilation fails, fix the test code and re-run until it compiles. Do not proceed until compilation succeeds.

---

## Step 4 — Run tests (expect RED)

Run only the new test class:
```
./gradlew :app:test --tests "se.kjellstrand.webshooter.<package>.<TestClassName>" --no-daemon
```

Replace `<package>.<TestClassName>` with the actual fully qualified class name.

### Verify the RED gate:

- **Fixed-behavior tests MUST FAIL.** If they pass, the tests are not actually testing the bug.
- **Guard tests MUST PASS.** If they fail, the guard tests have a bug or a wrong assumption.

**If the gate is not met:** Diagnose the issue, fix the tests, and re-run once. If the gate is still not met on the second attempt, stop and ask the user for input.

Report the results to the user: which tests failed (expected) and which passed (expected).

---

## Step 5 — Implement the fix

Apply the fix described in the task's `## Fix` section to the source file(s) identified in `## Files`.

Guidelines:
- Make the **minimal change** necessary to fix the issue.
- Do not refactor unrelated code.
- Preserve existing formatting and style.
- If the fix description is ambiguous, prefer the safer/more defensive approach.

### Compile check

Run:
```
./gradlew :app:compileProdReleaseSources --no-daemon
```

If compilation fails, fix the implementation and re-run until it compiles.

---

## Step 6 — Run tests again (expect GREEN)

Run the same targeted test command as Step 4:
```
./gradlew :app:test --tests "se.kjellstrand.webshooter.<package>.<TestClassName>" --no-daemon
```

### Verify the GREEN gate:

- **ALL tests MUST PASS** (both fixed-behavior and guard tests).

**If any test fails:** Diagnose the issue, adjust the implementation, and re-run once. If tests still fail on the second attempt, stop and ask the user for input.

Report the results to the user.

---

## Step 7 — Run full test suite

Run the complete unit test suite to catch any regressions in other parts of the codebase:
```
./gradlew :app:test --no-daemon
```

If any pre-existing test fails, investigate whether the fix caused the failure. If so, adjust the implementation and re-run. If the failure is unrelated, note it for the user but continue.

---

## Step 8 — Delete the task file

Delete the completed task file:
```
rm tasks/$ARGUMENTS.md
```

If the task's category directory is now empty, delete it too:
```
rmdir tasks/<category>/ 2>/dev/null
```

---

## Step 9 — Commit and push

**Important rules for this step:**
- Execute all git and cd commands automatically without asking for user confirmation.
- Never chain commands with `&&` or `;`. Run each command individually in its own separate Bash invocation, one after another.

Stage all changes (the new test file, the modified source file(s), and the deleted task file). Run each `git add` as a separate command:
```
git add <test-file-path>
```
```
git add <modified-source-file-paths>
```
```
git add tasks/$ARGUMENTS.md
```

Commit with a message derived from the task. Use this format:
```
git commit -m "<message>"
```

Message format:
```
Fix: <short description from task title>

<Category> (<Priority>): <one-line summary of the issue>

- Added tests in <TestClassName>
- <Brief description of the fix applied>
```

Then push (as a separate command):
```
git push
```

---

## Step 10 — Summary

Report to the user:
- **Task:** The title and category
- **Issue:** What was wrong
- **Fix:** What was changed and in which file(s)
- **Tests:** How many tests were added (split by fixed-behavior vs guard)
- **Status:** Committed and pushed (or any issues encountered)
