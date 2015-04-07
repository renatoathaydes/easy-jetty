package com.athaydes.easyjetty.mapper

import groovy.transform.TupleConstructor
import spock.lang.Specification

class ObjectMapperGroupSpec extends Specification {

    def "Simple ObjectMapperGroup with no mappers should work by calling value.toString()"() {
        given: 'A Simple ObjectMapperGroup with no mappers added'
        def mapperGroup = new ObjectMapperGroup(true, true)

        when: 'example values are mapped to a String'
        def result = mapperGroup.map(value)

        then: 'the result is exactly the same as calling value.toString()'
        result == value.toString()

        where:
        value << ['Hello', 1, 2, 100, 0.1f, true, [40, 20]]
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
        new ObjectMapperGroup(true, true).map(null) == '<null>'
    }

    def "The user-provided nullString is used to map null if it is set"() {
        expect:
        new ObjectMapperGroup(true, false).withNullString('hi').map(null) == 'hi'
    }

}

@TupleConstructor
abstract class Animal {
    final String race
}

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

class WildCat extends Cat {}

@TupleConstructor
class DomesticCat extends Cat {
    String name
}

class StringObjectMapper extends ObjectSerializer<String> {
    @Override
    String map(String object) {
        'String:' + object
    }

    @Override
    Class<? extends String> getMappedType() { String }
}

class PersonObjectMapper extends ObjectSerializer<Person> {
    @Override
    String map(Person object) {
        object.name + '->' + object.age
    }

    @Override
    Class<? extends Person> getMappedType() { Person }
}

class AnimalObjectMapper extends ObjectSerializer<Animal> {
    @Override
    String map(Animal object) {
        "Animal --- ${object.race}"
    }

    @Override
    Class<? extends Animal> getMappedType() { Animal }
}
