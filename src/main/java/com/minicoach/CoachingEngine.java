package com.minicoach;

import java.util.List;

/** Context for State and Strategy. Question progression never changes the stage. */
public final class CoachingEngine {
    private CoachingState state=CoachingState.GOAL;
    private int index;
    private final QuestionStrategy strategy;
    public CoachingEngine(QuestionStrategy strategy) { this.strategy=strategy; }
    public CoachingState state() { return state; }
    public List<String> questions() { return strategy.questions(state); }
    public boolean exhausted() { return index>=questions().size(); }
    public String question() {
        if(exhausted()) throw new IllegalStateException("No more questions in this stage");
        return questions().get(index);
    }
    public boolean more() {
        if(!exhausted()) index++;
        return !exhausted();
    }
    public void next(boolean includeAction) { state=state.next(includeAction); index=0; }
    public void finish() { state=CoachingState.DONE; index=0; }
}
