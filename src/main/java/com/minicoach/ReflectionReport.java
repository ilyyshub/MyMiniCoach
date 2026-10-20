package com.minicoach;

import java.util.*;

/** Builder: callers select report sections; build produces an immutable snapshot. */
public record ReflectionReport(String text) {
    public static final class Builder {
        private final Session session;
        private final List<String> sections = new ArrayList<>();
        public Builder(Session session) { this.session=session; }
        public Builder summary() {
            StringBuilder b=new StringBuilder("MY REFLECTION\nTopic: ").append(session.topic).append("\n");
            for(CoachingState stage:List.of(CoachingState.GOAL,CoachingState.INSIGHT,CoachingState.ACTION)) {
                var answers=session.answers().stream().filter(a -> a.stage().equals(stage.name()) && !a.text().isBlank()).toList();
                if(answers.isEmpty()) continue;
                String heading=switch(stage) {
                    case GOAL -> "What I wanted from this reflection";
                    case INSIGHT -> "What I chose to take away";
                    default -> "What I considered doing next";
                };
                b.append("\n").append(heading).append("\n");
                for(var a:answers) b.append("- ").append(a.text()).append("\n");
            }
            long count=session.answers().stream().filter(a -> !a.text().isBlank()).count();
            b.append("\n").append(count).append(count==1 ? " answer recorded.\n" : " answers recorded.\n");
            b.append("Your full question-and-answer history is available to review.\n");
            sections.add(b.toString()); return this;
        }
        public Builder ratings() {
            StringBuilder b=new StringBuilder("EMOTIONAL RATINGS (recorded by you)\n");
            var rated=session.answers().stream().filter(a -> a.intensity()!=null)
                .sorted(Comparator.comparingInt((Session.Answer a) -> a.intensity()).reversed()).toList();
            if (rated.isEmpty()) return this;
            for (var a:rated) b.append(a.intensity()).append("/5 — ").append(a.question()).append("\n");
            sections.add(b.toString()); return this;
        }
        public Builder transcript() {
            StringBuilder b=new StringBuilder("QUESTION & ANSWER HISTORY\n");
            int number=0;
            for (var a:session.answers()) {
                b.append("\n").append(++number).append(". [").append(CoachingState.valueOf(a.stage()).label()).append("] ").append(a.question()).append("\n")
                    .append(a.text().isEmpty() ? "[Skipped]" : a.text()).append("\n");
                if (a.intensity()!=null) b.append("Intensity: ").append(a.intensity()).append("/5\n");
            }
            sections.add(b.toString()); return this;
        }
        public ReflectionReport build() { return new ReflectionReport(String.join("\n", sections)); }
    }
}
