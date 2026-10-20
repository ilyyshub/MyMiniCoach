# Question sources and coverage

Source: the user-supplied **Coaching - Question-Bag.PDF**, a 12-page Russian excerpt headed “Приложение 2. Инструменты коучинга — чемоданы вопросов.” Full publication metadata was not supplied; no author, edition, or license is inferred. The PDF itself is not copied into the repository.

The user explicitly selected: **self-coaching; best ten questions; GROW (Goal, Reality, Choice, Will); and giving yourself feedback.** These correspond to Bags 1, 4, 5, and 7. Bags 2 (working agreements), 3 (permission), and 6 (progress debrief) are excluded. Bag 5's reference to Bag 6 does not expand the chosen scope.

## Coverage audit

Page numbers are PDF pages, not printed book numbers. Every question entry from the selected sections is represented. Commentary and declarative examples are not counted as questions.

| Section | PDF pages | Entries | Counting detail |
| --- | --- | ---: | --- |
| Self-coaching | 2 | 20 | All 20 bullets. |
| Best ten | 3–4 | 11 | Ten numbered questions plus the alternative prompt within item 2. Compound question 10 is kept together. |
| GROW / Goal | 4–5 | 35 | Seven conversation questions and 28 situation questions. The six subquestions about an imagined three-month outcome are separate entries, each with its context repeated. |
| GROW / Reality | 5–6 | 29 | Four questions on p. 5 and 25 on p. 6. |
| GROW / Choice | 6–7 | 28 | Seven questions on p. 6 and 21 on p. 7. |
| GROW / Will | 7–8 | 37 | Seven questions on p. 7 and 30 on p. 8. |
| Self-feedback / Goal | 11 | 2 | Questions addressed to the person reflecting. |
| Self-feedback / Reality | 11 | 6 | All positive-observation questions. |
| Self-feedback / Choice | 11–12 | 10 | Nine reflection questions plus the coach's permission question adapted to considering one's own suggestion. |
| Self-feedback / Will (lessons) | 12 | 8 | Six learning questions and two next-use questions. |
| **Total source entries** | | **186** | Source duplicates retained in the inventory. |

The two app-written closing invitations are stored as `app-takeaway` and `app-action`, with bag/page 0, so they cannot be mistaken for translated source questions.

## Wording and adaptation

The application uses English translations with light self-coaching adjustments, not an official translation. No category is represented by only a small sample. Some important adjustments:

- Bag 1's desired-outcome question becomes “What would you like to take away from this reflection?”; its explanatory examples are not prerequisites.
- References to a coach/conversation become a reflection or conversation with oneself. Claims such as “I have experience in this area” are not made by the application.
- Conditional premises are made explicit: “If you have more than one goal...” rather than asserting that the user has two goals; “If you set aside half an hour...” rather than claiming a timer exists.
- The six three-month visualization questions each repeat enough context to remain meaningful when preceding questions were skipped.
- The unfinished Reality prompt “How much...?” is completed generically as a question about quantities in the aspect being considered.
- In Best ten, the “I don't know” variant is preserved as a conditional invitation. The app does not infer that the user is uncertain.
- Coach-support and accountability questions are phrased around another person the user might choose, without pretending that a coach is present.
- Bag 7's coach statements (“I noticed...”, “I would like...”, etc.) are not questions and are not presented as generated observations. Its one coach permission question is retained as a self-feedback invitation.
- The self-coaching commitment question retains the source's option to do nothing for now and return later.

Bag 7's learning questions map to Reflection; its two next-use questions map to the optional Action stage. The app's Perspective stage hosts source Choice questions. The underlying inventory retains original section names even when the interface uses these gentler labels.

## Presentation rules

All source entries remain in `questions.tsv`, with a stable ID, bag number, original section, PDF page, assigned app stage, and English question. The generated `QUESTION_INVENTORY.md` shows the same rows for human review.

Opening sequences use a few explicitly ordered IDs, followed by every other question in that stage. Self-feedback prioritizes Bag 7. No question is filtered out by interpreting an answer. Exact repeated wording is shown once per stage; the source occurrences are not deleted. Near-duplicates remain distinct because the user requested the complete selected sets.

Introductions, transitions, and the two optional closing invitations are app text from the approved redesign. The resulting flow is adapted from GROW, with an explicit Reflection stage and optional Action; it is not claimed to be an exact reproduction of the original method.
