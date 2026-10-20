package com.minicoach;

import java.nio.file.*;
import java.util.*;

/** Dependency-free behavioral tests; run with ./test.sh. */
public final class CoachTest {
    private static int checks;
    private static void check(boolean condition,String message) {
        checks++; if(!condition) throw new AssertionError(message);
    }
    private static void rejects(Runnable action,String message) {
        try { action.run(); } catch(IllegalArgumentException | IllegalStateException expected) { checks++; return; }
        throw new AssertionError(message);
    }
    private static String console(Path directory,String... lines) {
        var bytes=new java.io.ByteArrayOutputStream();
        java.io.PrintStream previous=System.out;
        try {
            System.setOut(new java.io.PrintStream(bytes,true,java.nio.charset.StandardCharsets.UTF_8));
            new Main(new Scanner(String.join("\n",lines)+"\n"),directory).run();
        } finally { System.setOut(previous); }
        return bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
    }
    private static void consoleScenarios() throws Exception {
        Path dir=Files.createTempDirectory("minicoach-console-");
        try {
            String out=console(dir,"1","1","A fictional project","Clarify my priorities","continue",
                "/next","A description of the situation","", "/skip","A feeling I noticed","/rate 4","/review","/next",
                "/skip","/next","My own takeaway","/next","1","3","1","0");
            check(!out.contains("Unknown command") && !out.contains("Press Enter to continue here"),"Typed continue is accepted after an answer");
            check(out.contains("Rating recorded."),"Voluntary rating works without an emotional survey");
            var store=new SessionStore(dir); Session saved=store.load(store.files().getFirst());
            var reality=saved.answers().stream().filter(a -> a.stage().equals("REALITY")).toList();
            check(reality.size()==3,"Answer, continue and skip remain in Reality");
            check(reality.get(0).question().equals("What is happening at the moment?"),"Opening Reality question preserved");
            check(reality.get(1).question().equals("What concerns you most about this situation?") && reality.get(1).text().isEmpty(),"Skip records only the skipped Reality question");
            check(reality.get(2).question().equals("How do you feel about it?") && reality.get(2).intensity()==4,"Skip immediately reaches another Reality question");
            check(saved.answers().stream().noneMatch(a -> a.stage().equals("ACTION")),"Finishing after Reflection omits Action");
            check(out.contains("My own takeaway") && !out.contains("Status: Completed") && !out.contains("Finished early"),"Closing does not grade the reflection");
            out=console(dir,"2","1","0");
            check(out.contains("A description of the situation") && out.contains("QUESTION & ANSWER HISTORY"),"Saved transcript can be reopened");
            out=console(dir,"1","2","Feedback test","/next","A strength","/next","/next","/next","2","A small action","/next","2","0");
            check(out.contains("What did you like about what you did") && out.contains("Optional next step") && out.contains("A small action"),"Self-feedback and optional Action flow work");
            out=console(dir,"1","1","Finish test","/skip","/rate 3","/finish","2","0");
            check(out.contains("Skipped questions cannot be rated") && !out.contains("What I chose to take away"),"Finish never demands an insight, and skips cannot be rated");
            List<String> input=new ArrayList<>(List.of("1","1","Exhaustion"));
            for(int i=0;i<new QuestionStrategy.Explore().questions(CoachingState.GOAL).size();i++) input.add("/skip");
            input.addAll(List.of("", "/review", "/next", "/finish", "2", "0"));
            out=console(dir,input.toArray(String[]::new));
            check(out.contains("That's the end of the questions") && out.contains("No more questions here"),"Exhaustion offers explicit choices and no looped questions");
            check(out.indexOf("Reality\n")>out.indexOf("QUESTION & ANSWER HISTORY"),"Exhaustion does not silently advance");
            out=console(dir,"1","1","EOF test","An answer");
            check(out.contains("Reflection not saved."),"EOF exits without an infinite loop or silent save");
            out=console(dir,"1","1","Historical rating","First answer","","Second answer","/rate 1 5","/rate 9","/finish","3","2","0");
            check(out.contains("Rating recorded.") && out.contains("Intensity: 5/5") && out.contains("Could not record that rating"),"History rating and range validation work");
            out=console(dir,"1","1","Closing text","/finish","2","0");
            check(!out.contains("EMOTIONAL RATINGS") && !out.contains("QUESTION & ANSWER HISTORY"),"Unrequested ratings and transcript do not clutter the closing");
        } finally {
            try(var paths=Files.walk(dir)) { for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); }
        }
    }
    public static void main(String[] args) throws Exception {
        Map<Integer,Long> expectedCounts=Map.of(0,2L,1,20L,4,11L,5,129L,7,26L);
        var actual=QuestionBank.all().stream().collect(java.util.stream.Collectors.groupingBy(QuestionBank.Question::bag,java.util.stream.Collectors.counting()));
        check(actual.equals(expectedCounts),"Every selected source section and app invitation is present");
        check(QuestionBank.all().stream().allMatch(q -> expectedCounts.containsKey(q.bag())),"Excluded bags do not leak into the library");
        check(QuestionBank.all().stream().allMatch(q -> q.bag()==0 || q.page()>=2 && q.page()<=12),"Source questions carry page references");
        for(QuestionStrategy strategy:List.of(new QuestionStrategy.Explore(),new QuestionStrategy.Review())) {
            CoachingEngine engine=new CoachingEngine(strategy);
            check(engine.state()==CoachingState.GOAL,"Starts at goal");
            while(engine.state()!=CoachingState.DONE) {
                CoachingState stage=engine.state();
                Set<String> offered=new HashSet<>();
                while(!engine.exhausted()) {
                    check(offered.add(engine.question()),"No repeated question wording within a stage");
                    engine.more();
                    check(engine.state()==stage,"Answer/skip never advances the stage");
                }
                var expectedText=QuestionBank.all().stream().filter(q -> q.stage()==stage).map(QuestionBank.Question::text).collect(java.util.stream.Collectors.toSet());
                check(offered.equals(expectedText),"All selected questions remain reachable in both flows");
                check(!engine.more() && engine.state()==stage,"Exhaustion does not repeat or advance");
                rejects(engine::question,"No invalid question access after exhaustion");
                engine.next(true);
            }
            engine.next(true); check(engine.state()==CoachingState.DONE,"Done is terminal");
        }
        check(CoachingState.INSIGHT.next(false)==CoachingState.DONE,"Action is optional");
        CoachingEngine engine=new CoachingEngine(new QuestionStrategy.Explore());
        engine.next(false);
        check(engine.question().equals("What is happening at the moment?"),"Reality begins with the approved opener");
        engine.more();
        check(engine.question().equals("What concerns you most about this situation?"),"Skipping offers another Reality question");
        engine.finish(); check(engine.state()==CoachingState.DONE,"Can finish at any time");
        check(!new QuestionStrategy.Explore().questions(CoachingState.REALITY).getFirst().equals(new QuestionStrategy.Review().questions(CoachingState.REALITY).getFirst()),"Strategies open with different questions");
        Session s=new Session("Quotes \" and slash \\ and newline\nRomână 🌱", "Explore");
        rejects(() -> s.rateLast(2),"Cannot rate before answering");
        s.add("GOAL","Question?","First answer\nSecond line\t\u0001"); s.rateLast(5);
        rejects(() -> s.rateLast(0),"Reject low rating"); rejects(() -> s.rateLast(6),"Reject high rating");
        s.add("REALITY","Skipped?",""); rejects(() -> s.rateLast(2),"Cannot rate skipped question");
        s.completed=true;
        Path dir=Files.createTempDirectory("minicoach-test-");
        try {
            SessionStore store=new SessionStore(dir);
            Path saved=store.save(s); Session loaded=store.load(saved);
            check(loaded.topic.equals(s.topic),"Unicode and escaped topic round trip");
            check(loaded.answers().equals(s.answers()),"Answers and rating round trip");
            check(loaded.completed && loaded.id.equals(s.id),"Metadata round trip");
            store.save(s); check(store.files().size()==1,"Saving same session updates one file");
            String summary=new ReflectionReport.Builder(loaded).summary().build().text();
            check(summary.contains("First answer") && !summary.contains("QUESTION & ANSWER HISTORY"),"Summary can be built alone");
            String full=new ReflectionReport.Builder(loaded).summary().ratings().transcript().build().text();
            check(full.contains("5/5") && full.contains("[Skipped]") && full.contains("Question?"),"Complete report preserves transcript and ratings");
            Files.writeString(saved,"{broken");
            try { store.load(saved); throw new AssertionError("Invalid file accepted"); } catch(java.io.IOException expected) { checks++; }
            Files.writeString(saved,"{\"version\":2}");
            try { store.load(saved); throw new AssertionError("Unknown version accepted"); } catch(java.io.IOException expected) { checks++; }
        } finally {
            try(var paths=Files.walk(dir)) { for(Path p:paths.sorted(Comparator.reverseOrder()).toList()) Files.delete(p); }
        }
        rejects(() -> Json.read("{\"a\":1,\"a\":2}"),"Duplicate keys rejected");
        rejects(() -> Json.read("[] trailing"),"Trailing content rejected");
        rejects(() -> Json.read("\"unterminated"),"Unterminated string rejected");
        rejects(() -> Json.read("[01]"),"Invalid number rejected");
        consoleScenarios();
        System.out.println("Passed "+checks+" behavioral checks, including library coverage and console scenarios.");
    }
}
