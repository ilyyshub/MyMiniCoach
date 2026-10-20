package com.minicoach;

/** State defines the stage's introduction and the transition explicitly requested by the user. */
public enum CoachingState {
    GOAL("Goal", "Before exploring the situation, consider what would make this reflection useful to you.",
            "explore the situation") {
        public CoachingState next(boolean action) { return REALITY; }
    },
    REALITY("Reality", "This part offers space to look at what is happening. Some questions may fit better than others; you can pass over any that don't help.",
            "explore other perspectives") {
        public CoachingState next(boolean action) { return PERSPECTIVE; }
    },
    PERSPECTIVE("Perspective", "If you're ready, these questions invite you to look beyond your current view. You don't need to choose a solution.",
            "gather what you want to keep") {
        public CoachingState next(boolean action) { return INSIGHT; }
    },
    INSIGHT("Reflection", "You can look back over your answers with /review. There's no need to have reached a new insight.",
            "finish here or consider a next step") {
        public CoachingState next(boolean action) { return action ? ACTION : DONE; }
    },
    ACTION("Optional next step", "A next step can be small. You can also leave this undecided.", "finish here") {
        public CoachingState next(boolean action) { return DONE; }
    },
    DONE("Closing", "Here is what you chose to record.", "finish here") {
        public CoachingState next(boolean action) { return DONE; }
    };
    private final String label, introduction, transition;
    CoachingState(String label,String introduction,String transition) {
        this.label=label; this.introduction=introduction; this.transition=transition;
    }
    public String label() { return label; }
    public String introduction() { return introduction; }
    public String transition() { return transition; }
    public abstract CoachingState next(boolean action);
}
