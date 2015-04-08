package com.athaydes.easyjetty.mapper

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.eclipse.jetty.http.HttpHeader
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

class ObjectMapperGroupSpec extends Specification {

    def "Simple ObjectMapperGroup with no mappers should map by calling value.toString()"() {
        given: 'A Simple ObjectMapperGroup with no mappers added'
        def mapperGroup = new ObjectMapperGroup()

        when: 'example values are mapped to a String'
        def result = mapperGroup.map(value)

        then: 'the result is exactly the same as calling value.toString()'
        result == value.toString()

        where:
        value << ['Hello', 1, 2, 100, 0.1f, true, [40, 20]]
    }

    def "Simple ObjectMapperGroup with no mappers should be able to unmap Strings"() {
        given: 'A Simple ObjectMapperGroup with no mappers added'
        def mapperGroup = new ObjectMapperGroup()

        and: 'A Stubbed request whose content is given by an example'
        def request = Stub(HttpServletRequest)
        request.getReader() >> new BufferedReader(new StringReader(value as String))

        when: 'the request is unmapped'
        def result = mapperGroup.unmap(request, String, 999999)

        then: 'the result is exactly the same as calling value.toString()'
        result == value.toString()

        where:
        value << ['', 'Hello', 1, 2, 100, 0.1f, true, [40, 20]]
    }

    def "Non-lenient ObjectMapperGroup should throw an Exception for values whose type does not have an exact ObjectMapper"() {
        given: 'A non-lenient ObjectMapperGroup with a PersonObjectMapper'
        def mapperGroup = new ObjectMapperGroup(true, false)
                .withMappers(new PersonObjectMapper())

        when: 'example values that are not a Person are mapped to a String'
        mapperGroup.map(value)

        then: 'an Exception is thrown'
        thrown RuntimeException

        where:
        value << ['Hello', 1, 2, 100, 0.1f, true, [40, 20]]
    }

    def "Non-lenient ObjectMapperGroup with no mappers should NOT be able to unmap Strings"() {
        given: 'A non-lenient ObjectMapperGroup with no mappers added'
        def mapperGroup = new ObjectMapperGroup(false, false)

        and: 'A Stubbed request whose content is given by an example'
        def request = Stub(HttpServletRequest)
        request.getReader() >> new BufferedReader(new StringReader(value as String))

        when: 'the request is unmapped'
        mapperGroup.unmap(request, String, 999999)

        then: 'an Exception is thrown'
        thrown RuntimeException

        where:
        value << ['Hello', 1, 2, 100, 0.1f, true, [40, 20]]
    }

    def "Non-lenient ObjectMapperGroup should be able to map all Objects for which a Mapper exists"() {
        given: 'A non-lenient ObjectMapperGroup with a PersonObjectMapper and a StringObjectMapper'
        def mapperGroup = new ObjectMapperGroup(true, false)
                .withMappers(new PersonObjectMapper(), new StringObjectMapper())

        when: 'example values that are either a Person or a String are mapped to a String'
        def result = mapperGroup.map(value)

        then: 'the result is as expected'
        result == expected

        where:
        value                             | expected
        ''                                | 'String:'
        'hej'                             | 'String:hej'
        new Person(name: 'Mike', age: 32) | 'Mike->32'
    }

    def "Non-lenient ObjectMapperGroup should be able to unmap all Objects for which a Mapper exists"() {
        given: 'A non-lenient ObjectMapperGroup with a PersonObjectMapper and an AnimalObjectMapper'
        def mapperGroup = new ObjectMapperGroup(false, false)
                .withMappers(new PersonObjectMapper(), new AnimalObjectMapper())

        and: 'A Stubbed request whose content is a Person'
        def personReq = Stub(HttpServletRequest)
        personReq.getHeader(HttpHeader.ACCEPT.asString()) >> ObjectMapper.ACCEPT_EVERYTHING
        personReq.getReader() >> new BufferedReader(new StringReader(value as String))

        and: 'A Stubbed request whose content is a Person'
        def animalReq = Stub(HttpServletRequest)
        animalReq.getHeader(HttpHeader.ACCEPT.asString()) >> ObjectMapper.ACCEPT_EVERYTHING
        animalReq.getReader() >> new BufferedReader(new StringReader(value as String))

        when: 'example values unmapped to a Person and to an Animal'
        def person = mapperGroup.unmap(personReq, Person, 9999999)
        def cat = mapperGroup.unmap(animalReq, DomesticCat, 9999999)

        then: 'the result is as expected for both Person and Animal'
        person == expectedPerson
        cat == expectedAnimal

        where:
        value  | expectedPerson                    | expectedAnimal
        'hej'  | new Person(name: 'hej', age: -1)  | new DomesticCat(name: 'hej')
        'Mike' | new Person(name: 'Mike', age: -1) | new DomesticCat(name: 'Mike')
    }

    def "Non-exact-type ObjectMapperGroup should be able to map all Objects for which a Mapper exists for any of its super-types"() {
        given: 'A non-exact-type ObjectMapperGroup with a AnimalObjectMapper and PersonObjectMapper'
        def mapperGroup = new ObjectMapperGroup(false, false)
                .withMappers(new AnimalObjectMapper(), new PersonObjectMapper())

        when: 'example values that are of a sub-type of Animal to a String'
        def result = mapperGroup.map(value)

        then: 'the result is as expected from the AnimalMapper'
        result == expected

        where:
        value                             | expected
        new WildCat()                     | 'Animal --- cat'
        new DomesticCat(name: 'Tom')      | 'Animal --- cat'
        new Person(name: 'John', age: 30) | 'John->30'
    }

    def "The default nullString is used to map null"() {
        expect:
        new ObjectMapperGroup().map(null) == '<null>'
    }

    def "The user-provided nullString is used to map null if it is set"() {
        expect:
        new ObjectMapperGroup().withNullString('hi').map(null) == 'hi'
    }

    def "Requests for specific content-types can be unmapped if a Mapper exists for that"() {
        given: 'A Stubbed request with JSON'
        def request = Stub(HttpServletRequest)
        request.getReader() >> new BufferedReader(new StringReader('example'))
        request.getHeader(HttpHeader.ACCEPT.asString()) >> 'text/json'

        and: 'A non-lenient MapperGroup which has a mapper for JSON'
        def mapper = Stub(ObjectMapper)
        mapper.contentType >> 'text/json'
        mapper.mappedType >> String
        mapper.unmap('example') >> 'example result'
        def mapperGroup = new ObjectMapperGroup(false, false).withMappers(mapper)

        when:
        def result = mapperGroup.unmap(request, String, 999999)

        then:
        result == 'example result'
    }

    def "Requests for specific content-types cannot be unmapped if a Mapper does not exist for that"() {
        given: 'A Stubbed request with JSON'
        def request = Stub(HttpServletRequest)
        request.getReader() >> new BufferedReader(new StringReader('json content'))
        request.getHeader(HttpHeader.ACCEPT.asString()) >> 'text/json'

        and: 'A non-lenient MapperGroup which has a mapper only for XML'
        def mapper = Stub(ObjectMapper)
        mapper.contentType >> 'text/xml'
        mapper.mappedType >> String
        mapper.unmap(_) >> 'XML content'
        def mapperGroup = new ObjectMapperGroup(false, false).withMappers(mapper)

        when:
        mapperGroup.unmap(request, String, 999999)

        then:
        thrown RuntimeException
    }

}

@EqualsAndHashCode
abstract class Animal {
    final String race

    Animal(String race) {
        this.race = race
    }
}

@EqualsAndHashCode
class Person extends Animal {
    String name
    int age

    Person(Map args) {
        super('human')
        this.name = args.name
        this.age = args.age
    }
}

abstract class Cat extends Animal {
    Cat() { super('cat') }
}

@EqualsAndHashCode
class WildCat extends Cat {}

@EqualsAndHashCode
@TupleConstructor
@ToString
class DomesticCat extends Cat {
    String name
}

class StringObjectMapper extends ObjectSerializer<String> {

    final Class mappedType = String

    @Override
    String map(String object) {
        'String:' + object
    }

    @Override
    String unmap(String objectAsString) {
        objectAsString
    }
}

class PersonObjectMapper extends ObjectSerializer<Person> {

    final Class mappedType = Person

    @Override
    String map(Person object) {
        object.name + '->' + object.age
    }

    @Override
    Person unmap(String objectAsString) {
        new Person(name: objectAsString, age: -1)
    }
}

class AnimalObjectMapper extends ObjectSerializer<Animal> {

    final Class mappedType = Animal

    @Override
    String map(Animal object) {
        "Animal --- ${object.race}"
    }

    @Override
    Animal unmap(String objectAsString) {
        new DomesticCat(objectAsString)
    }
}
