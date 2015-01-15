package com.athaydes.easyjetty

import spock.lang.Specification

/**
 *
 */
class PathSanitizerTest extends Specification {

    def "Can match paths correctly"() {
        when:
        def result = PathSanitizer.matchParams(paramsByIndex, requestPath)
        then:
        result == expectedResult

        where:
        paramsByIndex                  | requestPath     | expectedResult
        [:]                            | 'someone/hello' | [:]
        [0: ':hi']                     | 'hello'         | ['hi': 'hello']
        [1: ':hi']                     | 'someone/hello' | ['hi': 'hello']
        [0: ':hi', 1: ':oi']           | 'someone/hello' | ['hi': 'someone', 'oi': 'hello']
        [0: ':hi', 3: ':ho', 4: ':hu'] | 'A/b/c/D/e'     | ['hi': 'A', 'ho': 'D', 'hu': 'e']

    }

}
