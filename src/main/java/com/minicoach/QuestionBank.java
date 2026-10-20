package com.minicoach;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Full source inventory. Source duplicates are retained here, not repeatedly asked. */
public final class QuestionBank {
    public record Question(String id, int bag, String section, int page, CoachingState stage, String text) {}
    private static final List<Question> ALL = load();
    private QuestionBank() {}
    public static List<Question> all() { return ALL; }
    private static List<Question> load() {
        var stream=QuestionBank.class.getResourceAsStream("questions.tsv");
        if(stream==null) throw new IllegalStateException("Question library is missing. Run ./run.sh to build with resources.");
        try(var reader=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8))) {
            List<Question> questions=new ArrayList<>();
            Set<String> ids=new HashSet<>();
            reader.readLine();
            String line;
            while((line=reader.readLine())!=null) {
                String[] fields=line.split("\t",-1);
                if(fields.length!=6 || !ids.add(fields[0]) || fields[5].isBlank())
                    throw new IllegalArgumentException("Invalid or duplicate question row: "+line);
                questions.add(new Question(fields[0],Integer.parseInt(fields[1]),fields[2],
                    Integer.parseInt(fields[3]),CoachingState.valueOf(fields[4]),fields[5]));
            }
            return List.copyOf(questions);
        } catch(IOException | IllegalArgumentException e) { throw new IllegalStateException("Cannot load question library",e); }
    }
    /** Explicit opening order, then the complete stage inventory in source order. */
    static List<String> sequence(CoachingState stage, boolean feedback) {
        String[] opening = switch(stage) {
            case GOAL -> feedback ? new String[]{"b7-001","b7-002"} : new String[]{"b1-002","b5-004","b5-003"};
            case REALITY -> feedback ? new String[]{"b7-003","b7-004"} : new String[]{"b5-036","b5-050","b5-040","b5-043","b1-009","b5-057"};
            case PERSPECTIVE -> feedback ? new String[]{"b7-009","b7-010","b7-011"} : new String[]{"b4-005","b4-004","b5-070"};
            case INSIGHT -> new String[]{"app-takeaway"};
            case ACTION -> new String[]{"app-action"};
            case DONE -> new String[]{};
        };
        LinkedHashSet<String> text=new LinkedHashSet<>();
        for(String id:opening) {
            var q=ALL.stream().filter(x -> x.id().equals(id)).findFirst().orElseThrow();
            if(q.stage()!=stage) throw new IllegalStateException("Opening question in wrong stage: "+id);
            text.add(q.text());
        }
        if(feedback) ALL.stream().filter(q -> q.stage()==stage && q.bag()==7).forEach(q -> text.add(q.text()));
        ALL.stream().filter(q -> q.stage()==stage).forEach(q -> text.add(q.text()));
        return List.copyOf(text);
    }
}
