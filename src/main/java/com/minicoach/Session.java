package com.minicoach;

import java.time.Instant;
import java.util.*;

/** One reflection, including skipped questions and user-requested ratings. */
public final class Session {
    public record Answer(String stage, String question, String text, Integer intensity) {
        public Answer {
            Objects.requireNonNull(stage); Objects.requireNonNull(question); Objects.requireNonNull(text);
            if (intensity != null && (intensity < 1 || intensity > 5))
                throw new IllegalArgumentException("Intensity must be 1–5");
        }
    }
    final String id, createdAt, topic, flow;
    private final List<Answer> answers = new ArrayList<>();
    boolean completed;
    public Session(String topic, String flow) {
        this(UUID.randomUUID().toString(), Instant.now().toString(), topic, flow);
    }
    Session(String id, String createdAt, String topic, String flow) {
        this.id=id; this.createdAt=createdAt; this.topic=topic; this.flow=flow;
    }
    public void add(String stage, String question, String text) {
        answers.add(new Answer(stage, question, text, null));
    }
    public void rateLast(int value) { rateAnswer(answers.size()-1,value); }
    public void rateAnswer(int index,int value) {
        if(index<0 || index>=answers.size()) throw new IllegalArgumentException("Choose an answered question number from /review.");
        Answer a=answers.get(index);
        if(a.text().isEmpty()) throw new IllegalStateException("Skipped questions cannot be rated.");
        answers.set(index,new Answer(a.stage(),a.question(),a.text(),value));
    }
    public List<Answer> answers() { return List.copyOf(answers); }
}
