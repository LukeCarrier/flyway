/**
 * Copyright (C) 2010-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.flyway.core.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for ClassPathResource.
 */
public class ClassPathResourceSmallTest {
    @Test
    public void getFilename() throws Exception {
        assertEquals("Mig777__Test.sql", new ClassPathResource("Mig777__Test.sql").getFilename());
        assertEquals("Mig777__Test.sql", new ClassPathResource("folder/Mig777__Test.sql").getFilename());
    }
}
