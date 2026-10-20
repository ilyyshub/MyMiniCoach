package com.minicoach;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class Main {
    private final Scanner input;
    private final SessionStore store;
    Main(Scanner input,Path directory) { this.input=input; store=new SessionStore(directory); }
    public static void main(String[] args) {
        new Main(new Scanner(System.in),args.length>0 ? Path.of(args[0]) : Path.of("data","sessions")).run();
    }
    private String read(String prompt) {
        System.out.print(prompt);
        return input.hasNextLine() ? input.nextLine().strip() : null;
    }
    void run() {
        System.out.println("\nMyMiniCoach — a little space to think\n");
        while(true) {
            String choice=read("1 Start reflection   2 View saved reflections   0 Exit\n> ");
            if(choice==null || choice.equals("0")) return;
            switch(choice) {
                case "1" -> reflect();
                case "2" -> history();
                default -> System.out.println("Choose 1, 2, or 0.");
            }
        }
    }
    private void reflect() {
        String choice;
        do {
            choice=read("1 Explore a situation   2 Give myself feedback   0 Back\n> ");
            if(choice==null || choice.equals("0")) return;
        } while(!choice.equals("1") && !choice.equals("2"));
        QuestionStrategy strategy=choice.equals("2") ? new QuestionStrategy.Review() : new QuestionStrategy.Explore();
        System.out.println("\nTake a little time to explore something on your mind. You don't need to solve it today.");
        System.out.println("You can leave any question unanswered.");
        String topic;
        do { topic=read("\nWhat would you like to reflect on?\n> "); if(topic==null) return; } while(topic.isBlank());
        Session session=new Session(topic,strategy.name());
        CoachingEngine engine=new CoachingEngine(strategy);
        System.out.println("\n/skip offers another question here. /next moves to the next stage. /finish closes your reflection.");
        System.out.println("Use /help for other options, including reviewing answers and adding an optional emotional rating.");
        CoachingState announced=null;
        boolean answered=false, showQuestion=true, reachedClosing=false;
        while(engine.state()!=CoachingState.DONE) {
            if(announced!=engine.state()) {
                announced=engine.state();
                System.out.println("\n"+announced.label()+"\n"+announced.introduction());
                System.out.println("When you're ready, /next will "+announced.transition()+".");
                showQuestion=true; answered=false;
            }
            boolean exhausted=engine.exhausted();
            if(showQuestion) {
                if(exhausted) System.out.println("\nThat's the end of the questions in this section. You can review your answers, move on, or finish here.");
                else System.out.println("\n"+engine.question());
                showQuestion=false;
            }
            String entry=read(exhausted ? "/review · /next · /finish\n> "
                : answered ? "Continue when you're ready (Enter) · /next · /help\n> " : "> ");
            if(entry==null) { engine.finish(); break; }
            // Plain words are aliases only at action prompts; ordinary answers remain literal.
            String command=entry.toLowerCase(Locale.ROOT);
            if(answered || exhausted) {
                command=switch(command) {
                    case "", "continue", "more" -> "/more";
                    case "next" -> "/next";
                    case "finish" -> "/finish";
                    case "review" -> "/review";
                    case "help" -> "/help";
                    case "skip" -> "/skip";
                    default -> command;
                };
            }
            if(command.equals("/finish")) { engine.finish(); break; }
            if(command.equals("/next")) {
                if(engine.state()==CoachingState.INSIGHT) {
                    Boolean action=chooseAction();
                    if(action==null) { engine.finish(); break; }
                    engine.next(action);
                } else engine.next(false);
                reachedClosing=engine.state()==CoachingState.DONE;
                answered=false; showQuestion=true; continue;
            }
            if(command.equals("/help")) { help(); continue; }
            if(command.equals("/review")) { System.out.println(transcript(session)); continue; }
            if(command.startsWith("/rate")) { rate(session,command); continue; }
            if(command.equals("/skip") || command.equals("/more")) {
                if(exhausted) { System.out.println("No more questions here. Use /next to move on, or /finish."); continue; }
                if(!answered) session.add(engine.state().name(),engine.question(),"");
                engine.more(); answered=false; showQuestion=true; continue;
            }
            if(entry.startsWith("/")) { System.out.println("Unknown command. Use /help to see the options."); continue; }
            if(answered || exhausted) { System.out.println("Press Enter to continue here, or use /next or /finish."); continue; }
            if(entry.isBlank()) { System.out.println("Take your time. You can write an answer, /skip, or /next."); continue; }
            session.add(engine.state().name(),engine.question(),entry);
            answered=true;
        }
        session.completed=reachedClosing; // Kept for compatibility; not used to grade a reflection in the UI.
        close(session);
    }
    private Boolean chooseAction() {
        while(true) {
            String answer=read("\n1 Finish here   2 Consider a next step\n> ");
            if(answer==null) return null;
            if(Set.of("1","n","no","finish","/finish").contains(answer.toLowerCase(Locale.ROOT))) return false;
            if(Set.of("2","y","yes","action").contains(answer.toLowerCase(Locale.ROOT))) return true;
            System.out.println("Choose 1 to finish here or 2 to consider a next step.");
        }
    }
    private void help() {
        System.out.println("""
            /skip     Pass over this question and see another in the same stage.
            /more     Another question here (or press Enter after answering).
            /next     Move to the next stage when you feel ready.
            /review   Read your answers so far, with question numbers.
            /rate 4   Record emotional intensity 1–5 for the latest answer.
            /rate 2 4 Record intensity 4 for question 2 in your history.
            /finish   Finish at any point and choose whether to save.
            Ratings are optional and never change the question sequence.
            """);
    }
    private void rate(Session session,String command) {
        try {
            String[] parts=command.split("\\s+");
            if(!parts[0].equals("/rate") || (parts.length!=2 && parts.length!=3))
                throw new IllegalArgumentException("Use /rate 1–5, or /rate QUESTION_NUMBER 1–5.");
            int index=parts.length==3 ? Integer.parseInt(parts[1])-1 : session.answers().size()-1;
            session.rateAnswer(index,Integer.parseInt(parts[parts.length-1]));
            System.out.println("Rating recorded.");
        } catch(IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not record that rating: "+e.getMessage());
        }
    }
    private static String transcript(Session s) { return new ReflectionReport.Builder(s).transcript().build().text(); }
    private void close(Session session) {
        System.out.println("\nHere is what you chose to record.\n");
        System.out.println(new ReflectionReport.Builder(session).summary().ratings().build().text());
        while(true) {
            String save=read("1 Save reflection   2 Leave without saving   3 Review all answers\n> ");
            if(save==null || Set.of("2","n","no").contains(save.toLowerCase(Locale.ROOT))) {
                System.out.println("Reflection not saved."); return;
            }
            if(Set.of("3","/review","review").contains(save.toLowerCase(Locale.ROOT))) { System.out.println(transcript(session)); continue; }
            if(!Set.of("1","y","yes").contains(save.toLowerCase(Locale.ROOT))) {
                System.out.println("Choose 1, 2, or 3."); continue;
            }
            try { System.out.println("Saved locally: "+store.save(session).toAbsolutePath()); return; }
            catch(IOException e) { System.out.println("Could not save: "+e.getMessage()+"\nYou can retry; your reflection is still here."); }
        }
    }
    private void history() {
        try {
            var sessions=new ArrayList<Session>();
            for(Path path:store.files()) {
                try { sessions.add(store.load(path)); }
                catch(IOException e) { System.out.println("Skipped unreadable file: "+path.getFileName()); }
            }
            sessions.sort(Comparator.comparing((Session s) -> s.createdAt).reversed());
            if(sessions.isEmpty()) { System.out.println("No saved reflections yet."); return; }
            for(int i=0;i<sessions.size();i++) System.out.println((i+1)+". "+sessions.get(i).topic+" — "+sessions.get(i).createdAt);
            while(true) {
                String number=read("Reflection number to read (0 to return): ");
                if(number==null || number.equals("0")) return;
                try {
                    int n=Integer.parseInt(number);
                    if(n<1 || n>sessions.size()) throw new NumberFormatException();
                    Session s=sessions.get(n-1);
                    System.out.println(new ReflectionReport.Builder(s).summary().ratings().transcript().build().text()); return;
                } catch(NumberFormatException e) { System.out.println("Choose a number from the list."); }
            }
        } catch(IOException e) { System.out.println("Could not read saved reflections: "+e.getMessage()); }
    }
}
