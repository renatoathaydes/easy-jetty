package com.athaydes.easyjetty

import com.athaydes.easyjetty.http.MethodArbiter
import spock.lang.Specification

class PathTreeTest extends Specification {


    def "put() specification"() {
        when:
        def tree = new PathTree()
        for (value in values) {
            tree.put(key as HandlerPath, value)
        }

        then:
        tree.get(key as HandlerPath) == values

        where:
        key                         | values
        []                          | [0]
        ['a']                       | [1, 2, 3]
        ['a', 'b']                  | [3]
        ['hello', 'world', 'cruel'] | [400, 400]
    }

    def "put() specification (with params)"() {
        when:
        def tree = new PathTree()
        tree.putAll(
                ([':a'] as HandlerPath): 1,
                ([':a', 'b'] as HandlerPath): 2,
                (['hello', ':world', 'cruel'] as HandlerPath): 400,
                (['hello', ':world', ':cruel'] as HandlerPath): 500,
                ([':hello', ':world', ':cruel'] as HandlerPath): 600)

        then:
        tree.get(queryPath as HandlerPath) == values

        where:
        queryPath                | values
        ['x']                    | [1]
        ['x', 'b']               | [2]
        ['hello', 'b', 'cruel']  | [400]
        ['hello', 'c', 'd']      | [500]
        ['why', 'where', 'when'] | [600]
    }

    def "get() specification - simple paths"() {
        given:
        def tree = new PathTree()
        tree.putAll(
                ([] as HandlerPath): 'empty',
                (['a'] as HandlerPath): 'just a',
                (['a', 'b'] as HandlerPath): 'a and b',
                (['a', 'b', 'c'] as HandlerPath): 'a and b and c',
                (['c'] as HandlerPath): 'just c',
                (['x', 'y', 'z'] as HandlerPath): 'x, y, z')

        expect:
        tree.get(path as HandlerPath) == expected

        where:
        path                 | expected
        []                   | ['empty']
        ['a']                | ['just a']
        ['a', 'b']           | ['a and b']
        ['a', 'b', 'c']      | ['a and b and c']
        ['c']                | ['just c']
        ['x', 'y', 'z']      | ['x, y, z']
        ['c', 'd']           | []
        ['x', 'y']           | []
        ['x']                | []
        ['x', 'y', 'z', 'w'] | []
        ['f']                | []
    }

    def "get() specification - paths with parameters"() {
        given:
        def tree = new PathTree()
        tree.putAll([
                ([] as HandlerPath)                              : 'empty',
                (['a'] as HandlerPath)                           : 'just a',
                ([':param1'] as HandlerPath)                     : 'just param1',
                (['b'] as HandlerPath)                           : 'just b',
                (['a', 'b'] as HandlerPath)                      : 'a and b',
                (['a', ':param2'] as HandlerPath)                : 'a and param2',
                ([':p1', ':p2', ':p3', ':p4'] as HandlerPath)    : '4 parameters',
                (['c', 'd', 'e'] as HandlerPath)                 : 'c and d and e',
                (['c', ':param3', 'e'] as HandlerPath)           : 'c and param3 and e',
                (['c', ':param3', 'e', ':param4'] as HandlerPath): 'c p3 e p4',
                ([':p1', 'b', ':p2', 'e'] as HandlerPath)        : 'p1 b p2 e',
        ])

        expect:
        tree.get(path as HandlerPath) == expected

        where:
        path                      | expected
        []                        | ['empty']
        ['a']                     | ['just a']
        ['c']                     | ['just param1']
        ['b']                     | ['just b']
        ['a', 'b']                | ['a and b']
        ['a', 'c']                | ['a and param2']
        ['a', 'xyz']              | ['a and param2']
        ['c', 'd', 'e']           | ['c and d and e']
        ['c', 'x', 'e']           | ['c and param3 and e']
        ['c', 'a', 'e']           | ['c and param3 and e']
        ['x', 'y', 'z', 'w']      | ['4 parameters']
        ['c', 'd', 'e', 'f']      | ['c p3 e p4']
        ['c', 'x', 'e', 'z']      | ['c p3 e p4']
        ['c', 'd', 'e', 'z']      | ['c p3 e p4']
        ['a', 'b', 'd', 'e']      | ['p1 b p2 e']
        ['x', 'b', 'x', 'e']      | ['p1 b p2 e']
        ['x', 'y', 'z', 'w', 'm'] | []
        ['a', 'x', 'c']           | []
        ['b', 'x']                | []
        ['c', 'x']                | []
        ['c', 'x', 'y']           | []
        ['c', 'd', 'f']           | []
        ['a', 'd', 'f']           | []
        ['c', 'x', 'f', 'z', 'e'] | []

    }

    def "size() specification"() {
        given:
        def handlers = handlerItems.collect { it as HandlerPath }
        def map = [:]
        for (handler in handlers) {
            map[handler] = true
        }
        def tree = new PathTree()
        tree.putAll map

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
