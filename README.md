# MyMiniCoach

A Java console prototype for guided self-reflection, grounded in the user's selected sections of the coaching question bag. No AI, accounts, database, or runtime libraries are required.

## Run

Use JDK 21 or newer. From the project directory:

```sh
./run.sh
```

In IntelliJ IDEA, open `pom.xml` as a project, select a JDK of at least 21, and run `com.minicoach.Main`. Set the working directory to the project root. Maven is optional: the scripts compile directly with the JDK and copy the question resource into the classpath.

## How the reflection works

Choose **Explore a situation** or **Give myself feedback**, then describe your topic. Each stage begins with a short introduction:

**Goal → Reality → Perspective → Reflection → optional next step**

There is no fixed question quota. Answering, continuing, and skipping all stay in the same stage. Only `/next` moves forward. You can finish from any stage, without a takeaway or an action. Reaching the end of a section does not automatically move forward or repeat questions.

At a question:

- Write an answer.
- `/skip` records it as skipped and immediately offers another question in this stage.
- `/next` moves to the next stage.
- `/finish` closes your reflection and offers saving.

After answering, press Enter or type `continue` to see another question here. `/next` and `/finish` remain available. A compact prompt replaces the former repeated command menu.

Additional options are available through `/help`:

- `/review` shows the question-and-answer history with question numbers.
- `/rate 4` attaches an optional emotional intensity of 4/5 to the latest answer.
- `/rate 2 4` attaches intensity 4/5 to question 2 in the history.
- `/more` requests another question here; at an unanswered question it acts like `/skip`.

Ratings accept integers 1–5, cannot be attached to skipped questions, and never alter the question sequence. The source's commitment questions sometimes use 1–10; those are ordinary reflection questions, separate from emotional ratings.

The Reflection stage offers an optional takeaway. `/next` there lets you finish or choose an optional action stage. Closing shows your topic, recorded goals, takeaways and possible actions, with recorded ratings if any. The full transcript is a separate option. No interpretation is generated, and no completion score is displayed.

Words such as `continue`, `next`, and `finish` are accepted as shortcuts at action prompts. While answering a question, those words remain literal answers; use slash commands to navigate.

## Complete selected question library

The library includes all question entries in the selected sections:

| PDF section | Source entries |
| --- | ---: |
| Bag 1: Self-coaching | 20 |
| Bag 4: Best ten | 11, including the alternative wording of question 2 |
| Bag 5: GROW — Goal, Reality, Choice, Will | 129 |
| Bag 7: Feedback, adapted for self-feedback | 26 |
| **Total source entries** | **186** |

Two separately identified app invitations introduce the optional takeaway and action. The original question inventory, source pages, light English adaptations, and coverage rules are described in [QUESTION_SOURCES.md](docs/QUESTION_SOURCES.md). The complete inventory is readable in [QUESTION_INVENTORY.md](docs/QUESTION_INVENTORY.md).

Questions live in `src/main/resources/com/minicoach/questions.tsv`, outside the Java interaction code. The opening questions have a deliberate order. After those, every remaining question in the stage is reachable in source order. The self-feedback flow prioritizes Bag 7. Exact duplicate wording is offered once per stage, while all source occurrences remain in the inventory. Similar but differently worded questions remain available.

This prototype does not infer relevance from free-text answers. Users decide whether to answer, skip, stay, or move on.

## Saved reflections

Sessions are UTF-8 JSON files in `data/sessions/`, ignored by Git. Saving is explicitly offered at the end. Files are local plaintext, not encrypted. There is no autosave or resume feature; ending input before saving leaves the reflection unsaved.

Existing version-1 session files remain readable. Their stored completion flag is preserved for compatibility, but the interface does not describe a reflection as successful or incomplete based on that flag. Saves use a temporary file and rename; unreadable files are skipped with a message when viewing history.

To choose another location:

```sh
./run.sh /path/to/sessions
```

## Lab 1: SOLID principles

Read the [Lab 1 SOLID report](docs/SOLID_REPORT.md) for code locations, examples, and presentation guidance.

| Principle | Evidence in this project |
| --- | --- |
| Single Responsibility (SRP) | Progression, persistence, and report formatting are separated into `CoachingEngine`, `SessionStore`, and `ReflectionReport.Builder`. |
| Open/Closed (OCP) | Another `QuestionStrategy` implementation can be supplied without changing `CoachingEngine`; menu integration may still require changes. |
| Dependency Inversion (DIP) | `CoachingEngine` receives the `QuestionStrategy` interface through its constructor rather than constructing `Explore` or `Review`. |

These are three concrete examples, not a claim of perfect SOLID compliance throughout the application.

## Existing design patterns and Lab 2

Lab 2 concerns creational patterns. Builder is creational; State and Strategy are behavioral. The existing patterns below are architecture context, not the three SOLID principles assessed for Lab 1. Whether the current Builder is sufficient for Lab 2 depends on that lab's full requirements.


| Pattern | Implementation | Purpose |
| --- | --- | --- |
| State | `CoachingState`, `CoachingEngine` | A stage defines its introduction and explicit next transition. Only user navigation calls the transition; question progression stays within the stage. Enum constant bodies are the concrete states. |
| Strategy | `QuestionStrategy.Explore`, `QuestionStrategy.Review` | Two opening orders over the full selected library, using the same navigation engine. |
| Builder | `ReflectionReport.Builder` | Independently composes a concise closing summary, optional recorded ratings, or a complete transcript. |

These are three patterns across two GoF categories: behavioral and creational.

`QuestionBank` loads source-tagged questions. `Session` stores answers. `SessionStore` handles files. `Main` handles interaction. `Json` supports this application's schema and is not intended as a general-purpose JSON library.

## Verify

```sh
./test.sh
```

The dependency-free tests check full question coverage in both flows, absence of repeated wording, stage boundaries, exhaustion, skipping, explicit transitions, optional action, ratings, report selection, JSON round trips and malformed files. Console scenarios exercise typed `continue`, multiple questions within Reality, skip-to-another-question, self-feedback, finishing without insight, saving/reopening, and end-of-input. Each source entry is also checked for reachability in the applicable stage.

These tests run through `test.sh`, not Maven Surefire. Technical checks verify the agreed behavior; the coaching experience remains a prototype to refine through use.
