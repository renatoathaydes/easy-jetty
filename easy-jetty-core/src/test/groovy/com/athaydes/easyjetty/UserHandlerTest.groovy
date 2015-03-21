package com.athaydes.easyjetty

import spock.lang.Specification
import spock.lang.Unroll

class UserHandlerTest extends Specification {

    def "Accepted content types must be of the form type/subType"() {
        expect:
        UserHandler.parseAcceptedContentTypes(acceptDirective) == expectedResults

        where:
        acceptDirective                        | expectedResults
        'application/json'                     | ['application/json']
        'application/xml, application/json'    | ['application/xml', 'application/json']
        '  text/html '                         | ['text/html']
        'model/x3d+binary,text/html,text/json' | ['model/x3d+binary', 'text/html', 'text/json']
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
