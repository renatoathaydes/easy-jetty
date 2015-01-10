package com.athaydes.easyjetty;

import java.util.*;

/**
 * A Tree specialized for paths.
 */
public class PathTree<V> {

    private int size = 0;
    private final Node root = new Node("");

    public PathTree() {

    }

    public PathTree(Map<HandlerPath, V> map) {
        putAll(map);
    }

    public V put(HandlerPath key, V value) {
        Objects.requireNonNull(value);
        Node node = get(root, key, true);
        if (node.value == null) {
            size++;
        }
        V oldValue = node.value;
        node.value = value;
        return oldValue;
    }

    /**
     * @return a human-friendly String representing the keys and values stored
     * in this Tree.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tree {\n");
        toString(sb, root.children);
        sb.append("}");
        return sb.toString();
    }

    private void toString(StringBuilder sb, SortedMap<String, Node> children) {
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
            toString(sb, child.children);
        }
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size < 1;
    }

    public boolean containsKey(HandlerPath key) {
        Node node = get(root, key, false);
        return node != null;
    }

    public boolean containsValue(V value) {
        if (value == null) {
            return false;
        }
        return containsValue(root, value);
    }

    private boolean containsValue(Node start, V value) {
        if (start.value != null && start.value.equals(value)) {
            return true;
        }
        for (Node node : start.children.values()) {
            if (containsValue(node, value)) {
                return true;
            }
        }
        return false;
    }

    public V get(HandlerPath key) {
        Node node = get(root, key, false);
        return node == null ? null : node.value;
    }

    private Node get(Node start, HandlerPath key, boolean createIfAbsent) {
        if (start == null || key.size() == 0) {
            return start;
        }
        String target = key.head();
        Node child = start.children.get(target);
        if (child == null && createIfAbsent) {
            child = new Node(target);
            start.addChild(target, child);
        }
        return get(child, key.tail(), createIfAbsent);
    }

    public void putAll(Map<? extends HandlerPath, ? extends V> map) {
        for (Map.Entry<? extends HandlerPath, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
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
        for (Node child : start.children.values()) {
            values(result, child);
        }
    }


    private class Node implements Comparable<Node> {

        String key;
        Node parent;
        SortedMap<String, Node> children;
        V value;
        int depth;

        public Node(String key) {
            this.key = key;
            clear();
        }

        void clear() {
            this.children = new TreeMap<>();
            this.value = null;
            this.parent = null;
            this.depth = -1;
        }

        public void addChild(String key, Node child) {
            children.put(key, child);
            child.parent = this;
            child.depth = this.depth + 1;
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

}