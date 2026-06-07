package com.vcore.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * A trie (prefix tree) data structure for efficient string prefix matching and lookup.
 * <p>
 * Words can be inserted via {@link #add(String)} and then searched using {@link #search(String)}.
 * The search returns the shortest prefix of the input that was previously inserted as a complete
 * word, enabling longest-prefix-matching scenarios commonly needed in package name resolution
 * and routing within the virtual framework.
 * </p>
 */
public class TrieTree {
    /** The root node of the trie, which has no character content. */
    private final TrieNode root = new TrieNode();

    /**
     * Internal node in the trie. Each node holds a single character, a reference to its
     * parent's accumulated word prefix, and a flag indicating whether it terminates a
     * complete inserted word.
     */
    private static class TrieNode {
        /** The character this node represents. */
        char content;
        /** The accumulated word (prefix string) from the root to this node. */
        String word;
        /** Whether this node marks the end of an inserted word. */
        boolean isEnd = false;
        /** Child nodes sorted by insertion order. */
        final List<TrieNode> children = new LinkedList<>();

        /** Creates an empty root node with no character content. */
        public TrieNode() { }

        /**
         * Creates a node with the given character and accumulated word.
         *
         * @param content the character this node represents
         * @param word    the accumulated prefix string from root to this node
         */
        public TrieNode(char content, String word) {
            this.content = content;
            this.word = word;
        }

        /**
         * Equality is based solely on the character content, enabling child lookups
         * via {@link List#contains(Object)}.
         *
         * @param object the object to compare
         * @return {@code true} if the object is a {@link TrieNode} with the same character
         */
        @Override
        public boolean equals(Object object) {
            if (object instanceof TrieNode) {
                return ((TrieNode) object).content == content;
            }
            return false;
        }

        /**
         * Finds the child node with the given character.
         *
         * @param content the character to search for among children
         * @return the matching child node, or {@code null} if no child has that character
         */
        public TrieNode nextNode(char content) {
            for (TrieNode childNode : children) {
                if (childNode.content == content) {
                    return childNode;
                }
            }
            return null;
        }
    }

    /**
     * Inserts a word into the trie. Creates new nodes as needed for characters that
     * do not already exist in the tree. Shared prefixes among different words will
     * reuse the same node path.
     *
     * @param word the word to insert; must not be {@code null}
     */
    public void add(String word) {
        TrieNode current = root;
        StringBuilder wordBuilder = new StringBuilder();

        for (int index = 0; index < word.length(); ++index) {
            char content = word.charAt(index);
            wordBuilder.append(content);

            TrieNode node = new TrieNode(content, wordBuilder.toString());
            if (Objects.requireNonNull(current).children.contains(node)) {
                current = current.nextNode(content);
            } else {
                current.children.add(node);
                current = node;
            }

            if (index == (word.length() - 1)) {
                Objects.requireNonNull(current).isEnd = true;
            }
        }
    }

    /**
     * Searches the trie for a word match within the given input string. Returns the
     * accumulated word at the first node marked as a word-end, effectively performing
     * a shortest-prefix match among all previously inserted words.
     *
     * @param word the input string to search for a matching prefix
     * @return the matched word string if found, or {@code null} if no previously inserted
     *         word is a prefix of the input
     */
    public String search(String word) {
        TrieNode current = root;
        for (int index = 0; index < word.length(); ++index) {
            char content = word.charAt(index);

            TrieNode node = new TrieNode(content, null);
            if (current.children.contains(node)) {
                current = current.nextNode(content);
            } else {
                return null;
            }

            if (Objects.requireNonNull(current).isEnd) {
                return current.word;
            }
        }
        return null;
    }
}
