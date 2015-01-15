package com.athaydes.easyjetty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A Tree specialized for paths.
 */
public class PathTree<V> {

    private int size = 0;
    private final Node root = new Node("");

    public PathTree(Map<HandlerPath, V> map) {
        putAll(map);
    }

    private void putAll(Map<? extends HandlerPath, ? extends V> map) {
        for (Map.Entry<? extends HandlerPath, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    private void put(HandlerPath key, V value) {
        Objects.requireNonNull(value);

        Node child = root, current;
        while (key.size() > 0) {
            current = child;
            String target = key.head();
            key = key.tail();
            child = current.getExact(target);
            if (child == null) {
                if (PathSanitizer.isParam(target)) {
                    child = current.getParam(target);
                }
                if (child == null) {
                    child = new Node(target);
                    current.addChild(target, child);
                }
            }
        }

        if (child.value == null) {
            size++;
        }
        child.value = value;
    }

    /**
     * @return a human-friendly String representing the keys and values stored
     * in this Tree.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tree {\n");
        toString(sb, root.getChildren());
        sb.append("}");
        return sb.toString();
    }

    private void toString(StringBuilder sb, Map<String, Node> children) {
        class Indenter {
            Object indent(int depth) {
                depth++;
                String result = "";
                for (int i = 0; i < depth; i++) {
                    result += "  ";
                }
                return result;
            }
        }
        final Indenter indenter = new Indenter();
        for (Node child : children.values()) {
            sb.append(indenter.indent(child.depth)).append("key='").append(child.key).append("' ");
            if (child.value != null) {
                sb.append("value='").append(child.value).append("'");
            }
            sb.append("\n");
            toString(sb, child.getChildren());
        }
    }

    public int size() {
        return size;
    }

    public PathTreeValue<V> get(HandlerPath key) {
        return valueFrom(get(root, key));
    }

    private PathTreeValue<V> valueFrom(Node node) {
        return node == null || node.value == null ?
                (PathTreeValue<V>) PathTreeValue.NULL_VALUE :
                new PathTreeValue<>(node.value, Collections.<String, Object>emptyMap());
    }

    private Node get(Node start, HandlerPath key) {
        Node child = start;
        if (start != null && key.size() > 0) {
            String target = key.head();
            key = key.tail();
            child = get(start.getExact(target), key);
            if (child == null || child.value == null) {
                for (Node paramChild : start.params.values()) {
                    child = get(paramChild, key);
                    if (child != null && child.value != null) {
                        break;
                    }
                }
            }
        }
        return child;
    }

    public void clear() {
        root.clear();
        size = 0;
    }

    public Collection<V> values() {
        List<V> result = new ArrayList<>(size());
        values(result, root);
        return result;
    }

    private void values(List<V> result, Node start) {
        if (start.value != null) {
            result.add(start.value);
        }
        for (Node child : start.values()) {
            values(result, child);
        }
    }


    private class Node implements Comparable<Node> {

        String key;
        Node parent;
        private Map<String, Node> children;
        private Map<String, Node> params;
        V value;
        int depth;

        public Node(String key) {
            this.key = key;
            clear();
        }

        void clear() {
            this.children = new HashMap<>();
            this.params = new HashMap<>();
            this.value = null;
            this.parent = null;
            this.depth = -1;
        }

        public void addChild(String key, Node child) {
            if (PathSanitizer.isParam(key)) {
                params.put(key, child);
            } else {
                children.put(key, child);
            }
            child.parent = this;
            child.depth = this.depth + 1;
        }

        public Node getExact(String key) {
            return children.get(key);
        }

        public Node getParam(String param) {
            return params.get(param);
        }

        public Map<String, Node> getChildren() {
            Map<String, Node> result = new HashMap<>(children);
            result.putAll(params);
            return result;
        }

        public Collection<Node> values() {
            return getChildren().values();
        }

        @Override
        public int compareTo(Node o) {
            return this.key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return "Node [key=" + key + ", value=" + value + ", depth=" + depth
                    + "]";
        }

    }

    static class PathTreeValue<V> {

        final Map<String, Object> pathParameters;
        final V value;

        PathTreeValue(V value, Map<String, Object> pathParameters) {
            this.value = value;
            this.pathParameters = pathParameters;
        }

        static final PathTreeValue<?> NULL_VALUE =
                new PathTreeValue<>(null, Collections.<String, Object>emptyMap());

    }

}