package com.athaydes.easyjetty

import spock.lang.Specification

import static com.athaydes.easyjetty.PathTree.PathTreeValue.*

class PathTreeTest extends Specification {


    def tree = new PathTree()

    def "put() specification"() {
        when:
        def prev = tree.put(key as HandlerPath, value)

        then:
        prev == NULL_VALUE
        tree.get(key as HandlerPath)?.value == value

        where:
        key                         | value
        []                          | 0
        ['a']                       | 1
        ['a', 'b']                  | 2
        ['hello', 'world', 'cruel'] | 400
    }

    def "get() specification"() {
        given:
        tree.putAll([
                ([] as HandlerPath)             : 'empty',
                (['a'] as HandlerPath)          : 'just a',
                (['a', 'b'] as HandlerPath)     : 'a and b',
                (['a', 'b', 'c'] as HandlerPath): 'a and b and c',
                (['c'] as HandlerPath)          : 'just c',
                (['x', 'y', 'z'] as HandlerPath): 'x, y, z',
        ])

        expect:
        tree.get([] as HandlerPath)?.value == 'empty'
        tree.get(['a'] as HandlerPath)?.value == 'just a'
        tree.get(['a', 'b'] as HandlerPath)?.value == 'a and b'
        tree.get(['a', 'b', 'c'] as HandlerPath)?.value == 'a and b and c'
        tree.get(['c'] as HandlerPath)?.value == 'just c'
        tree.get(['x', 'y', 'z'] as HandlerPath)?.value == 'x, y, z'

        tree.get(['c', 'd'] as HandlerPath)?.value == null
        tree.get(['x', 'y'] as HandlerPath)?.value == null
        tree.get(['x'] as HandlerPath)?.value == null
        tree.get(['x', 'y', 'z', 'w'] as HandlerPath)?.value == null
        tree.get(['f'] as HandlerPath)?.value == null
    }

    def "size() specification"() {
        given:
        def handlers = handlerItems.collect { it as HandlerPath }
        def map = [:]
        for (handler in handlers) {
            map[handler] = true
        }
        tree.putAll(map)
        println tree.toString()

        expect:
        tree.size() == expected

        where:
        handlerItems                          || expected
        []                                    || 0
        [[]]                                  || 1
        [['a']]                               || 1
        [['a', 'b'], []]                      || 2
        [[], []]                              || 1
        [['a'], ['b'], ['c', 'd'], ['b'], []] || 4
    }

}
