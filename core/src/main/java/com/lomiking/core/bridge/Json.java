package com.lomiking.core.bridge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tiny dependency-free JSON reader/writer, enough for the bridge (maps, lists, strings, numbers, booleans, null).
 * Kept small on purpose so the core has no external libraries.
 */
public final class Json {
    private Json() { }

    // ------------------------------- writing -------------------------------
    public static String stringify(Object v) {
        StringBuilder sb = new StringBuilder();
        write(sb, v);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object v) {
        if (v == null) sb.append("null");
        else if (v instanceof String) writeString(sb, (String) v);
        else if (v instanceof Enum) writeString(sb, ((Enum<?>) v).name());
        else if (v instanceof Boolean) sb.append(v);
        else if (v instanceof Double || v instanceof Float) {
            double d = ((Number) v).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) sb.append("null");
            else if (d == Math.rint(d) && Math.abs(d) < 1e15) sb.append((long) d);
            else sb.append(d);
        }
        else if (v instanceof Number) sb.append(v);
        else if (v instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                writeString(sb, String.valueOf(e.getKey()));
                sb.append(':');
                write(sb, e.getValue());
            }
            sb.append('}');
        } else if (v instanceof Collection) {
            sb.append('[');
            boolean first = true;
            for (Object o : (Collection<?>) v) {
                if (!first) sb.append(',');
                first = false;
                write(sb, o);
            }
            sb.append(']');
        } else writeString(sb, v.toString());
    }

    private static void writeString(StringBuilder sb, String s) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        sb.append('"');
    }

    // ------------------------------- reading -------------------------------
    /** Parses JSON into Map / List / String / Double / Boolean / null. */
    public static Object parse(String text) {
        Parser p = new Parser(text);
        Object v = p.value();
        p.skipWs();
        if (p.pos != text.length()) throw p.error("Unexpected trailing characters");
        return v;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String text) {
        Object v = parse(text);
        if (!(v instanceof Map)) throw new IllegalArgumentException("Expected a JSON object");
        return (Map<String, Object>) v;
    }

    private static final class Parser {
        final String s;
        int pos;
        Parser(String s) { this.s = s; }

        IllegalArgumentException error(String msg) { return new IllegalArgumentException(msg + " at " + pos); }
        void skipWs() { while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++; }
        char peek() { skipWs(); if (pos >= s.length()) throw error("Unexpected end"); return s.charAt(pos); }
        void expect(char c) { if (peek() != c) throw error("Expected '" + c + "'"); pos++; }

        Object value() {
            char c = peek();
            if (c == '{') return object();
            if (c == '[') return array();
            if (c == '"') return string();
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            if (s.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
            if (s.startsWith("null", pos)) { pos += 4; return null; }
            return number();
        }
        Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            expect('{');
            if (peek() == '}') { pos++; return m; }
            while (true) {
                String k = string();
                expect(':');
                m.put(k, value());
                char c = peek(); pos++;
                if (c == '}') return m;
                if (c != ',') throw error("Expected ',' or '}'");
            }
        }
        List<Object> array() {
            List<Object> l = new ArrayList<>();
            expect('[');
            if (peek() == ']') { pos++; return l; }
            while (true) {
                l.add(value());
                char c = peek(); pos++;
                if (c == ']') return l;
                if (c != ',') throw error("Expected ',' or ']'");
            }
        }
        String string() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    char e = s.charAt(pos++);
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case 'u': sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16)); pos += 4; break;
                        default: sb.append(e);
                    }
                } else sb.append(c);
            }
            throw error("Unterminated string");
        }
        Double number() {
            int start = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            if (start == pos) throw error("Unexpected character '" + s.charAt(pos) + "'");
            return Double.parseDouble(s.substring(start, pos));
        }
    }
}
