package com.minicoach;

import java.util.*;

/** Small JSON codec for our versioned local data. No reflection or external dependencies. */
final class Json {
    static String write(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) {
            StringBuilder b=new StringBuilder("\"");
            for (char c:s.toCharArray()) {
                switch(c) {
                    case '"' -> b.append("\\\"");
                    case '\\' -> b.append("\\\\");
                    case '\n' -> b.append("\\n");
                    case '\r' -> b.append("\\r");
                    case '\t' -> b.append("\\t");
                    default -> { if(c<32) b.append(String.format("\\u%04x", (int)c)); else b.append(c); }
                }
            }
            return b.append('"').toString();
        }
        if (value instanceof Boolean || value instanceof Integer) return value.toString();
        if (value instanceof Map<?,?> map) {
            var parts=new ArrayList<String>();
            map.forEach((k,v) -> parts.add(write(k.toString())+":"+write(v)));
            return "{"+String.join(",",parts)+"}";
        }
        if (value instanceof List<?> list) return "["+String.join(",",list.stream().map(Json::write).toList())+"]";
        throw new IllegalArgumentException("Unsupported JSON value");
    }
    static Object read(String text) {
        Parser p=new Parser(text); Object value=p.value(0); p.space();
        if(p.pos!=text.length()) throw p.error();
        return value;
    }
    private static final class Parser {
        final String text; int pos;
        Parser(String text) { this.text=text; }
        IllegalArgumentException error() { return new IllegalArgumentException("Invalid JSON near position "+pos); }
        void space() { while(pos<text.length() && " \t\r\n".indexOf(text.charAt(pos))>=0) pos++; }
        boolean take(char c) { space(); if(pos<text.length() && text.charAt(pos)==c) { pos++; return true; } return false; }
        void need(char c) { if(!take(c)) throw error(); }
        Object value(int depth) {
            if(depth>30) throw error();
            space(); if(pos>=text.length()) throw error(); char c=text.charAt(pos);
            if(c=='"') return string();
            if(take('{')) {
                Map<String,Object> map=new LinkedHashMap<>();
                if(take('}')) return map;
                do { space(); String key=string(); need(':');
                    if(map.containsKey(key)) throw error(); map.put(key,value(depth+1));
                } while(take(','));
                need('}'); return map;
            }
            if(take('[')) {
                List<Object> list=new ArrayList<>();
                if(take(']')) return list;
                do { list.add(value(depth+1)); } while(take(','));
                need(']'); return list;
            }
            for(String literal:List.of("true","false","null")) {
                if(text.startsWith(literal,pos)) {
                    pos+=literal.length(); return literal.equals("null") ? null : literal.equals("true");
                }
            }
            int start=pos;
            if(pos<text.length() && text.charAt(pos)=='-') pos++;
            if(pos<text.length() && text.charAt(pos)=='0') pos++;
            else while(pos<text.length() && text.charAt(pos)>='0' && text.charAt(pos)<='9') pos++;
            try { return Integer.valueOf(text.substring(start,pos)); } catch(NumberFormatException e) { throw error(); }
        }
        String string() {
            need('"'); StringBuilder b=new StringBuilder();
            while(pos<text.length()) {
                char c=text.charAt(pos++);
                if(c=='"') return b.toString();
                if(c<32) throw error();
                if(c!='\\') { b.append(c); continue; }
                if(pos>=text.length()) throw error();
                char escape=text.charAt(pos++);
                switch(escape) {
                    case '"','\\','/' -> b.append(escape);
                    case 'n' -> b.append('\n'); case 'r' -> b.append('\r'); case 't' -> b.append('\t');
                    case 'b' -> b.append('\b'); case 'f' -> b.append('\f');
                    case 'u' -> {
                        if(pos+4>text.length()) throw error();
                        try { b.append((char)Integer.parseInt(text.substring(pos,pos+4),16)); }
                        catch(NumberFormatException e) { throw error(); }
                        pos+=4;
                    }
                    default -> throw error();
                }
            }
            throw error();
        }
    }
}
