# Laboratory Work 1 — SOLID Principles in MyMiniCoach

**Project:** MyMiniCoach — guided self-reflection application  
**Language:** Java 21  
**Focus:** Single Responsibility, Open/Closed, and Dependency Inversion  
**Date:** 20 October 2026

## 1. Objective

Demonstrate three of the five SOLID principles through concrete examples in an object-oriented application. This report explains the responsibilities, extension points, and dependency relationships already present in MyMiniCoach.

SOLID principles guide software design. They are not the same as design patterns. The examples assessed here are **SRP, OCP, and DIP**, rather than State, Strategy, and Builder.

## 2. Project context

MyMiniCoach is a console application that helps a user explore a situation or give themselves feedback using predefined coaching questions. It does not use AI or interpret free-text answers.

The reflection moves through **Goal → Reality → Perspective → Reflection → optional Action**. The user controls progression: skipping offers another question within the current stage, while moving to the next stage is a separate action. Users may review their answers, optionally record emotional intensity, finish at any point, and choose whether to save.

Questions are stored separately from Java code in a TSV resource. The library contains 186 source entries from the selected coaching-question sections and two app-specific closing invitations. Reflections are saved locally as JSON; summaries organize the user's own words.

### Main components

| Component | Responsibility |
| --- | --- |
| `Main` | Console interaction and coordinating the application |
| `CoachingEngine` | Tracking the stage and question position, and managing progression |
| `CoachingState` | Defining stage introductions and stage-specific transitions |
| `QuestionStrategy` | Defining the contract for naming a flow and supplying its questions |
| `QuestionStrategy.Explore` / `QuestionStrategy.Review` | Supplying exploration or self-feedback question ordering |
| `QuestionBank` | Loading question content and constructing ordered question lists |
| `Session` | Holding session metadata, answers, and optional ratings |
| `SessionStore` | Saving, listing, and loading session files |
| `ReflectionReport.Builder` | Assembling views of session data for display |
| `Json` | Encoding and parsing the JSON subset used by the application |

## 3. Single Responsibility Principle — SRP

**Definition:** A class should have one main responsibility and one coherent reason to change. This does not mean that a class must have only one method.

### Evidence in MyMiniCoach

The application separates session progression, persistence, and report formatting into different classes:

| Class | Responsibility | Example reason to change |
| --- | --- | --- |
| `CoachingEngine` | Maintain the current stage and question position | Change how navigation advances through questions |
| `SessionStore` | Persist and retrieve sessions | Change the stored file schema or save procedure |
| `ReflectionReport.Builder` | Construct report text | Change headings or which content a report section displays |

`SessionStore.save(Session)` writes session data to a JSON file. `SessionStore.load(Path)` reconstructs a session from a file. Saving and loading are two related operations within the same persistence responsibility.

`CoachingEngine`, in contrast, contains no file-writing or console-printing code. Its progression methods include:

```java
public boolean more() {
    if (!exhausted()) index++;
    return !exhausted();
}

public void next(boolean includeAction) {
    state = state.next(includeAction);
    index = 0;
}
```

The report builder produces text from the stored answers. It does not save files or control stage transitions.

**Practical benefit:** changing a report heading does not require editing the engine or JSON persistence code. Changing file handling does not require rewriting how the person moves between stages.

**Scope of the claim:** these classes demonstrate a useful separation of responsibilities. The report does not claim perfect SRP across the entire application: `Main` currently coordinates several console concerns and could be split further as the application grows.

**Short explanation for presentation:** “I separated progression, persistence, and report formatting. Each has its own reason to change, so changes in one area do not force changes in the others.”

## 4. Open/Closed Principle — OCP

**Definition:** Software should be open for extension but closed for modification at its intended extension points. New behavior should be addable without rewriting the stable code that uses it.

### Evidence in MyMiniCoach

Question selection is an extension point defined by this interface:

```java
public interface QuestionStrategy {
    String name();
    List<String> questions(CoachingState stage);
    // Existing nested implementations: Explore and Review.
}
```

The current implementations are:

- `Explore`: an ordering intended for exploring a situation.
- `Review`: an ordering that prioritizes self-feedback questions.

The engine obtains questions through the interface:

```java
public List<String> questions() {
    return strategy.questions(state);
}
```

It does not contain a branch such as “if this is exploration, select these questions; otherwise, select feedback questions.” That choice belongs outside the engine.

**Extension example:** a future developer could add another `QuestionStrategy` implementation for a different ordering policy and pass it to `CoachingEngine`. The existing engine could use it through the same interface without changes to its source code. This is an explanation of the extension point, not a claim that a third flow has already been implemented.

**Practical benefit:** new question-ordering behavior can be added without disturbing the code responsible for skipping questions and moving between stages.

**Scope of the claim:** `CoachingEngine` is closed to modification for this particular kind of extension. The menu in `Main` would still need updating to expose a new option, and a new flow may require new question content. Adding a new stage is a different change and may require modifications elsewhere. OCP does not mean that no file in the project may ever change.

**Short explanation for presentation:** “The engine uses the question interface. I can add another implementation without modifying the engine's navigation logic.”

## 5. Dependency Inversion Principle — DIP

**Definition:** High-level logic should depend on abstractions rather than directly on concrete implementation details. Concrete implementations should satisfy the abstraction expected by that logic.

### Evidence in MyMiniCoach

In the question-selection relationship:

- **Higher-level consumer:** `CoachingEngine`, which coordinates progression.
- **Abstraction:** `QuestionStrategy`.
- **Concrete implementations:** `QuestionStrategy.Explore` and `QuestionStrategy.Review`.

The engine stores an interface-typed dependency and receives it through its constructor:

```java
private final QuestionStrategy strategy;

public CoachingEngine(QuestionStrategy strategy) {
    this.strategy = strategy;
}
```

It does not construct `Explore` or `Review` internally. `Main` selects the implementation and supplies it:

```java
QuestionStrategy strategy = choice.equals("2")
    ? new QuestionStrategy.Review()
    : new QuestionStrategy.Explore();

CoachingEngine engine = new CoachingEngine(strategy);
```

The relationship is:

```text
CoachingEngine uses QuestionStrategy
Explore implements QuestionStrategy
Review implements QuestionStrategy
Main chooses and supplies an implementation
```

**Practical benefit:** the engine is not coupled to the implementation classes' names or their question-ordering details. It can also be exercised with a small test implementation of `QuestionStrategy` without needing console input or persistent files.

Constructor injection is the mechanism used to supply the dependency. It supports DIP here because the supplied dependency is an abstraction; simply passing a concrete class through a constructor would not, by itself, demonstrate DIP.

**Scope of the claim:** DIP is demonstrated at the engine's question-provider boundary. The whole application is not fully abstracted: for example, `Main` constructs a concrete `SessionStore`, and the existing strategy implementations call the concrete `QuestionBank` helper.

**Short explanation for presentation:** “The engine depends on the QuestionStrategy interface, not on Explore or Review. Main creates the chosen implementation and injects it into the engine.”

## 6. Why OCP and DIP use the same interface

The same design decision can support multiple principles:

| Principle | Question it answers | Evidence |
| --- | --- | --- |
| SRP | Are different responsibilities separated? | Progression, persistence, and formatting have distinct classes. |
| OCP | Can a new question policy be added without changing the engine? | Another implementation can satisfy the existing interface. |
| DIP | What does the engine depend on? | Its field and constructor refer to `QuestionStrategy`, not concrete policies. |

OCP describes the extension benefit. DIP describes the dependency structure that helps make that extension possible. They are related but not interchangeable.

## 7. Where to find the evidence in IntelliJ

Expand **src → main → java → com → minicoach**. The full directory is:

`/Users/ilincaciocanu/MyMiniCoach/src/main/java/com/minicoach/`

The following line numbers were verified against the current source. Method and class names are provided because line numbers may change after later edits.

| Principle | File and location | What to show |
| --- | --- | --- |
| SRP | `CoachingEngine.java`, line 6 | Progression class, with no file or console operations |
| SRP | `SessionStore.java`, line 7; `save` at line 10 and `load` at line 37 | Persistence responsibility |
| SRP | `ReflectionReport.java`, line 7 | Separate report-formatting responsibility |
| OCP | `QuestionStrategy.java`, line 6 | Extension interface |
| OCP | `QuestionStrategy.java`, lines 9 and 13 | Existing implementations that satisfy the interface |
| OCP | `CoachingEngine.java`, line 12 | Delegation without checking concrete strategy types |
| DIP | `CoachingEngine.java`, lines 9–10 | Interface-typed field and constructor injection |
| DIP | `Main.java`, line 36 and line 42 | Selection and injection of the concrete implementation |

Suggested presentation order: explain the class responsibilities first, show the strategy interface and its implementations second, then trace how `Main` passes the chosen object into `CoachingEngine`.

## 8. Verification and limits

Behavioral tests are in:

`/Users/ilincaciocanu/MyMiniCoach/src/test/java/com/minicoach/CoachTest.java`

The existing tests exercise the engine with both strategies, verify question progression and explicit stage transitions, test report composition, and check session persistence. These checks support confidence in the separated components, but automated behavior tests alone do not prove SOLID compliance; the principles are justified through the code relationships described above.

The test suite passed during the preceding implementation task. This report revision only updates documentation: no application behavior was changed, and the test suite was not rerun for the documentation edit.

To run the application or its tests from the project directory:

```sh
./run.sh
./test.sh
```

## 9. Lab scope and conclusion

For **Lab 1**, the project demonstrates three SOLID principles through specific existing code: **SRP** through separated responsibilities, **OCP** through the engine's question-policy extension point, and **DIP** through its dependence on the injected `QuestionStrategy` abstraction. The report does not claim the other two principles as assessed examples or assert perfect compliance across every class.

For **Lab 2**, the assignment concerns creational design patterns. The project already contains a report Builder; State and Strategy are behavioral patterns and do not count as creational examples. The required number and structure of creational patterns must be checked against the Lab 2 specification separately.
