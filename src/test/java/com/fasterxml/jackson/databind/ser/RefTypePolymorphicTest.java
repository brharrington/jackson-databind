package com.fasterxml.jackson.databind.ser;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;

/**
 * Verify that JsonTypeInfo annotation works with refrences to polymorphic types.
 */
public class RefTypePolymorphicTest extends BaseMapTest
{
    /*
    /*********************************************************
    /* Helper bean classes
    /*********************************************************
     */

    static class ContainerA {
        @JsonProperty private AtomicReference<Strategy> strategy =
                new AtomicReference<>((Strategy) new Foo(42));
    }

    static class ContainerB {
        @JsonProperty private AtomicReference<List<Strategy>> strategy =
                new AtomicReference<>(Collections.singletonList((Strategy) new Foo(42)));
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
    @JsonSubTypes({ @JsonSubTypes.Type(name = "Foo", value = Foo.class) })
    interface Strategy { }

    static class Foo implements Strategy {
        @JsonProperty private final int foo;

        @JsonCreator
        Foo(@JsonProperty("foo") int foo) {
            this.foo = foo;
        }
    }

    /*
    /*********************************************************
    /* Test cases
    /*********************************************************
     */

    private final ObjectMapper MAPPER = objectMapper();

    private final String EXPECTED = "{\"type\":\"Foo\",\"foo\":42}";

    public void testRefToPolymorphicType() throws Exception {
        String json = MAPPER.writeValueAsString(new ContainerA());
        Assert.assertEquals("{\"strategy\":" + EXPECTED + "}", json);
    }

    // Reproduction of issue seen with scala.Option and java8 Optional types:
    // https://github.com/FasterXML/jackson-module-scala/issues/346#issuecomment-336483326
    public void testRefToListOfPolymorphicType() throws Exception {
        String json = MAPPER.writeValueAsString(new ContainerB());
        Assert.assertEquals("{\"strategy\":[" + EXPECTED + "]}", json);
    }
}
