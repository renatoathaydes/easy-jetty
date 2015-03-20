package com.athaydes.easyjetty.extension

import com.athaydes.easyjetty.EasyJetty
import com.athaydes.easyjetty.extension.event.AfterStartEvent
import com.athaydes.easyjetty.extension.event.AfterStopEvent
import com.athaydes.easyjetty.extension.event.BeforeStartEvent
import com.athaydes.easyjetty.extension.event.BeforeStopEvent
import com.athaydes.easyjetty.extension.event.ExtensionAddedEvent
import spock.lang.Specification

class EasyJettyExtensionTest extends Specification {

    EasyJetty easy = new EasyJetty()

    def "Happy path events are called as appropriate"() {
        given: 'An extension'
        def extension = Mock(EasyJettyExtension)

        when: 'The extension is added to EasyJetty'
        easy.withExtension(extension)

        and: 'EasyJetty starts'
        easy.start()

        and: 'EasyJetty stops'
        easy.stop()

        then: 'The extension should receive the ExtensionAddedEvent'
        1 * extension.handleEvent(_ as ExtensionAddedEvent)

        and: 'The extension should receive the BeforeStartEvent and AfterStartEvent'
        1 * extension.handleEvent(_ as BeforeStartEvent)
        1 * extension.handleEvent(_ as AfterStartEvent)

        and: 'The extension should receive the BeforeStopEvent and AfterStopEvent'
        1 * extension.handleEvent(_ as BeforeStopEvent)
        1 * extension.handleEvent(_ as AfterStopEvent)
    }

}
