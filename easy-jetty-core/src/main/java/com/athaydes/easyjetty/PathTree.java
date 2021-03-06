package com.athaydes.easyjetty;

import java.util.*;

/**
 * A Tree specialized for paths.
 */
class PathTree<V> {

    private int size = 0;
    private final Node root = new Node("");

    void putAll(Map<HandlerPath, ? extends V> map) {
        for (Map.Entry<HandlerPath, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    void putFirst(HandlerPath key, V value) {
        put(key, value, true);
    }

    void put(HandlerPath key, V value) {
        put(key, value, false);
    }

    private void put(HandlerPath key, V value, boolean addFirst) {
        Objects.requireNonNull(value);

        Node child = root, current;
        while (key.size() > 0) {
            current = child;
            String target = key.head();
            key = key.tail();
            child = current.getExact(target);
            if (child == null) {
                if (PathHelper.isParam(target)) {
                    child = current.getParam();
                }
                if (child == null) {
                    child = new Node(target);
                    current.addChild(child);
                }
            }
        }

        size++;
        if (addFirst) {
            child.values.add(0, value);
        } else {
            child.values.add(value);
        }
    }

    /**
     * @return a human-friendly String representing the keys and childrenValues stored
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
            if (!child.values.isEmpty()) {
                sb.append("value='").append(child.values).append("'");
            }
            sb.append("\n");
            toString(sb, child.getChildren());
        }
    }

    public int size() {
        return size;
    }

    public List<V> get(HandlerPath key) {
        Node node = get(root, key);
        if (node == null) {
            return Collections.emptyList();
        } else {
            return node.values;
        }
    }

    private Node get(Node start, HandlerPath key) {
        Node child = start;
        if (start != null && key.size() > 0) {
            String target = key.head();
            key = key.tail();
            child = get(start.getExact(target), key);
            if (child == null || child.values.isEmpty()) {
                child = get(start.getParam(), key);
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
        result.addAll(start.values);
        for (Node child : start.childrenValues()) {
            values(result, child);
        }
    }


    private static class Node implements Comparable<Node> {

        private final String key;
        private Map<String, Node> children;
        private Node param;
        List values;
        int depth;

        public Node(String key) {
            this.key = key;
            clear();
        }

        void clear() {
            this.children = new HashMap<>(3);
            this.param = null;
            this.values = new LinkedList();
            this.depth = -1;
        }

        public void addChild(Node child) {
            if (PathHelper.isParam(child.key)) {
                if (param == null) {
                    param = child;
                }
            } else {
                children.put(child.key, child);
            }
            child.depth = this.depth + 1;
        }

        public Node getExact(String key) {
            return children.get(key);
        }

        public Node getParam() {
            return param;
        }

        public Map<String, Node> getChildren() {
            Map<String, Node> result = new HashMap<>(children);
            if (param != null) {
                result.put(":", param);
            }
            return result;
        }

        public Collection<Node> childrenValues() {
            return getChildren().values();
        }

        @Override
        public int compareTo(Node o) {
            return this.key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return "Node [key=" + key + ", values=" + values + ", depth=" + depth
                    + "]";
        }

    }

}