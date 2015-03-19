package com.athaydes.easyjetty

import spock.lang.Specification
import spock.lang.Unroll

class UserHandlerTest extends Specification {

    def "Accepted content types may include patterns such as type/subType1+subType2"() {
        expect:
        UserHandler.parseAcceptedContentTypes(acceptDirective) == expectedResults

        where:
        acceptDirective                        | expectedResults
        'application/json'                     | ['application/json']
        'application/xml+json'                 | ['application/xml', 'application/json']
        'text/html'                            | ['text/html']
        'text/html+xml+plain,application/html' | ['text/html', 'text/xml', 'text/plain', 'application/html']
    }

    @Unroll
    def "Should not accept invalid Accept directive: #acceptDirective"() {
        when:
        UserHandler.parseAcceptedContentTypes(acceptDirective)

        then:
        thrown RuntimeException

        where:
        acceptDirective << ['', 'application', '/', '/abc', 'a/b/c', 'a/b/',
                            '*', '*/*/', 'a/b,/c', 'a/b,*']
    }

}
