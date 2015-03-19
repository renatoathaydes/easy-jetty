package com.athaydes.easyjetty.extension

import com.athaydes.easyjetty.EasyJetty
import com.athaydes.easyjetty.extension.event.AfterStartEvent
import com.athaydes.easyjetty.extension.event.AfterStopEvent
import com.athaydes.easyjetty.extension.event.BeforeStartEvent
import com.athaydes.easyjetty.extension.event.BeforeStopEvent
import spock.lang.Specification

class EasyJettyExtensionTest extends Specification {

    EasyJetty easy = new EasyJetty()

    def "Happy path events are called as appropriate"() {
        given: 'An extension is added to EasyJetty'
        def extension = Mock(EasyJettyExtension)
        easy.withExtension(extension)

        when: 'EasyJetty starts'
        easy.start()

        and: 'EasyJetty stops'
        easy.stop()

        then: 'The extension should receive the BeforeStartEvent and AfterStartEvent'
        1 * extension.handleEvent(_ as BeforeStartEvent)
        1 * extension.handleEvent(_ as AfterStartEvent)

        and: 'The extension should receive the BeforeStopEvent and AfterStopEvent'
        1 * extension.handleEvent(_ as BeforeStopEvent)
        1 * extension.handleEvent(_ as AfterStopEvent)
    }

}
