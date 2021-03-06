/*
 * Copyright 2008 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a class/interface will be part of our our public api given to our customers.<p>
 *
 * It also means that anything with this annotation should have sufficient documentation and unit tests,
 * and most importantly <b>do not break or change the existing behavior of anything with this annotation</b><p>
 *
 * If a class is annotated, then all static/inner classes are automatically included
 * (this behavior is slightly different from the other annotations).
 * You should <em>not</em> use this annotation on static/inner classes, as doing so
 * will also expose the enclosing class, which is usually not the desired behavior.<p>
 *
 * Warning: do not use this annotation on a package or private classes class, such as:
 * <pre><code>
 *     public class Foo {
 *          // whatever
 *     }
 *
 *     &#x40;PublicApi  // wrong!
 *     class FooHelper {
 *
 *     }
 * </code></pre>
 *
 * Doing so, will cause the class to be included in the api (and will expose the code itself),
 * but it wont be accessible to anyone else.
 */
@Retention(RetentionPolicy.RUNTIME) // We need runtime for static analasys.
@Target({ ElementType.TYPE,
          ElementType.ANNOTATION_TYPE,
          ElementType.METHOD,
          ElementType.CONSTRUCTOR
          })
@PgPublicApi // not part of the api, but must be in the api jar
public @interface PgPublicApi {
    // marker annotation
}
