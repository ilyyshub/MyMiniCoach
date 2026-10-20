package com.minicoach;

import java.util.List;

/** Strategy changes the opening sequence, while keeping the complete selected library available. */
public interface QuestionStrategy {
    String name();
    List<String> questions(CoachingState stage);
    final class Explore implements QuestionStrategy {
        public String name() { return "Explore a situation"; }
        public List<String> questions(CoachingState stage) { return QuestionBank.sequence(stage,false); }
    }
    final class Review implements QuestionStrategy {
        public String name() { return "Give myself feedback"; }
        public List<String> questions(CoachingState stage) { return QuestionBank.sequence(stage,true); }
    }
}
