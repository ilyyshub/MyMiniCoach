package com.minicoach;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class SessionStore {
    private final Path directory;
    public SessionStore(Path directory) { this.directory=directory; }
    public Path save(Session s) throws IOException {
        Files.createDirectories(directory);
        Map<String,Object> data=new LinkedHashMap<>();
        data.put("version",1); data.put("id",s.id); data.put("createdAt",s.createdAt);
        data.put("topic",s.topic); data.put("flow",s.flow); data.put("completed",s.completed);
        List<Object> answers=new ArrayList<>();
        for(var a:s.answers()) {
            Map<String,Object> row=new LinkedHashMap<>();
            row.put("stage",a.stage()); row.put("question",a.question()); row.put("text",a.text()); row.put("intensity",a.intensity());
            answers.add(row);
        }
        data.put("answers",answers);
        Path destination=directory.resolve(UUID.fromString(s.id)+".json");
        Path temp=Files.createTempFile(directory,"session-",".tmp");
        try {
            Files.writeString(temp,Json.write(data)+"\n");
            try { Files.move(temp,destination,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING); }
            catch(AtomicMoveNotSupportedException e) { Files.move(temp,destination,StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temp); }
        return destination;
    }
    public List<Path> files() throws IOException {
        if(!Files.exists(directory)) return List.of();
        try(var files=Files.list(directory)) {
            return files.filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        }
    }
    public Session load(Path path) throws IOException {
        try {
            Map<?,?> data=(Map<?,?>)Json.read(Files.readString(path));
            if(!Integer.valueOf(1).equals(data.get("version"))) throw new IllegalArgumentException("Unsupported session version");
            String id=string(data,"id"); UUID.fromString(id);
            Session s=new Session(id,string(data,"createdAt"),string(data,"topic"),string(data,"flow"));
            s.completed=(Boolean)Objects.requireNonNull(data.get("completed"));
            for(Object row:(List<?>)data.get("answers")) {
                Map<?,?> a=(Map<?,?>)row;
                String stage=string(a,"stage"); CoachingState.valueOf(stage);
                s.add(stage,string(a,"question"),string(a,"text"));
                if(a.get("intensity")!=null) s.rateLast((Integer)a.get("intensity"));
            }
            return s;
        } catch(RuntimeException e) { throw new IOException("Session file is invalid: "+path.getFileName(),e); }
    }
    private static String string(Map<?,?> map,String key) { return (String)Objects.requireNonNull(map.get(key)); }
}
