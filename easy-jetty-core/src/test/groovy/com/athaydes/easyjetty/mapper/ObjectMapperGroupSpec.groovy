package com.athaydes.easyjetty.mapper

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.TupleConstructor
import org.eclipse.jetty.http.HttpHeader
import spock.lang.Specification

import javax.servlet.http.HttpServletRequest

import static com.athaydes.easyjetty.mapper.ObjectMapper.ACCEPT_EVERYTHING

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
        personReq.getHeader(HttpHeader.CONTENT_TYPE.asString()) >> ACCEPT_EVERYTHING
        personReq.getReader() >> new BufferedReader(new StringReader(value as String))

        and: 'A Stubbed request whose content is a Person'
        def animalReq = Stub(HttpServletRequest)
        animalReq.getHeader(HttpHeader.CONTENT_TYPE.asString()) >> ACCEPT_EVERYTHING
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

    def "A lenient ObjectMapperGroup should be able to map all Collections of Objects for which a Mapper exists"() {
        given: 'A lenient ObjectMapperGroup with a PersonObjectMapper and a StringObjectMapper'
        def mapperGroup = new ObjectMapperGroup(true, true)
                .withMappers(new PersonObjectMapper(), new StringObjectMapper())

        when: 'example values that are a Collection of Persons or Strings are mapped to a String'
        def result = mapperGroup.map(value)

        then: 'the result is as expected'
        result == expected

        where:
        value                               | expected
        []                                  | '[]'
        ['', 'ho']                          | '[String:, String:ho]'
        ['hej']                             | '[String:hej]'
        [new Person(name: 'Mike', age: 32)] | '[Mike->32]'
    }

    def "A lenient ObjectMapperGroup should be able to map form contents to Map without any ObjectMappers added"() {
        given: 'A lenient ObjectMapperGroup without any ObjectMappers added'
        def mapperGroup = new ObjectMapperGroup(true, true)

        when: 'unmapping example values that are Strings in html form format'
        Map result = mapperGroup.unmap(value, Map, 'application/x-www-form-urlencoded')

        then: 'the result is a Map with the form entries'
        result.isEmpty() && expected.isEmpty() || result == expected

        where:
        value                                                      | expected
        ''                                                         | [:]
        'a=1'                                                      | ['a': '1']
        'Name=Jonathan+Doe&Age=23&Formula=a+%2B+b+%3D%3D+13%25%21' |
                ['Name': 'Jonathan Doe', 'Age': '23', 'Formula': 'a + b == 13%!']
    }

    def "A ObjectMapperGroup should be able to map all Collections of a type for which a Mapper exists"() {
        given: 'A ObjectMapperGroup with a PersonObjectMapper and a StringObjectMapper'
        def mapperGroup = new ObjectMapperGroup(false, false)
                .withCollectionMappers(new CollectionMapperParams(ACCEPT_EVERYTHING, ' ; ', '<<', '>>'))
                .withMappers(new PersonObjectMapper(), new StringObjectMapper())

        when: 'example values that are a Collection of Persons or Strings are mapped to a String'
        def result = mapperGroup.map(value)

        then: 'the result is as expected'
        result == expected

        where:
        value                               | expected
        []                                  | '<<>>'
        ['', 'ho']                          | '<<String: ; String:ho>>'
        ['hej']                             | '<<String:hej>>'
        [new Person(name: 'Mike', age: 32)] | '<<Mike->32>>'
    }

    def "A ObjectMapperGroup should be able to unmap all Collections of Objects for which a Mapper exists"() {
        given: 'A non-lenient ObjectMapperGroup with a PersonObjectMapper and an AnimalObjectMapper'
        def mapperGroup = new ObjectMapperGroup(false, false)
                .withCollectionMappers(new CollectionMapperParams(ACCEPT_EVERYTHING, ' ; ', '<<', '>>'))
                .withMappers(new PersonObjectMapper(), new AnimalObjectMapper())

        and: 'A Stubbed request whose content is a Collection of Person'
        def personReq = Stub(HttpServletRequest)
        personReq.getHeader(HttpHeader.ACCEPT.asString()) >> ACCEPT_EVERYTHING
        personReq.getReader() >> new BufferedReader(new StringReader(value as String))

        and: 'A Stubbed request whose content is a Person'
        def animalReq = Stub(HttpServletRequest)
        animalReq.getHeader(HttpHeader.ACCEPT.asString()) >> ACCEPT_EVERYTHING
        animalReq.getReader() >> new BufferedReader(new StringReader(value as String))

        when: 'example values unmapped to a Person and to an Animal'
        def persons = mapperGroup.unmapAll(personReq, Person, 9999999)
        def cats = mapperGroup.unmapAll(animalReq, DomesticCat, 9999999)

        then: 'the result is as expected for both Person and Animal'
        persons == expectedPersons
        cats == expectedAnimals

        where:
        value          | expectedPersons                     | expectedAnimals
        '<<hej ; ho>>' | [new Person(name: 'hej', age: -1),
                          new Person(name: 'ho', age: -1)]   |
                [new DomesticCat(name: 'hej'),
                 new DomesticCat(name: 'ho')]
        '<<Mike>>'     | [new Person(name: 'Mike', age: -1)] | [new DomesticCat(name: 'Mike')]
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
        request.getHeader(HttpHeader.CONTENT_TYPE.asString()) >> 'text/json'

        and: 'A non-lenient MapperGroup which has a mapper for JSON'
        def mapper = Stub(ObjectMapper)
        mapper.contentType >> 'text/json'
        mapper.mappedType >> String
        mapper.unmap('example', String) >> 'example result'
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

    @Override
    def <S> S unmap(String objectAsString, Class<S> type) {
        type.cast(unmap(objectAsString))
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

    @Override
    def <S> S unmap(String objectAsString, Class<S> type) {
        type.cast(unmap(objectAsString))
    }
}
