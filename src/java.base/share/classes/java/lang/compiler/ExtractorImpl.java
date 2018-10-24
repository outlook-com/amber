/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.lang.compiler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;

/**
 * Non-public implementation of {@link Extractor}
 */
class ExtractorImpl implements Extractor {
    private final MethodType descriptor;
    private final boolean partial;
    private final MethodHandle tryMatch;
    private final List<MethodHandle> components;

    /**
     * Construct an {@link Extractor} from components
     * Constraints:
     *  - output of tryMatch must match input of components
     *  - input of tryMatch must match descriptor
     *  - output of components must match descriptor
     *
     * @param descriptor The {@code descriptor} method type
     * @param tryMatch The {@code tryMatch} method handle
     * @param components The {@code component} method handles
     */
    ExtractorImpl(MethodType descriptor, boolean partial, MethodHandle tryMatch, MethodHandle[] components) {
        Class<?> carrierType = tryMatch.type().returnType();
        if (descriptor.parameterCount() != components.length)
            throw new IllegalArgumentException(String.format("MethodType %s arity should match component count %d", descriptor, components.length));
        if (!descriptor.returnType().equals(tryMatch.type().parameterType(0)))
            throw new IllegalArgumentException(String.format("Descriptor %s should match tryMatch input %s", descriptor, tryMatch.type()));
        for (int i = 0; i < components.length; i++) {
            MethodHandle component = components[i];
            if (component.type().parameterCount() != 1
                || component.type().returnType().equals(void.class)
                || !component.type().parameterType(0).equals(carrierType))
                throw new IllegalArgumentException("Invalid component descriptor " + component.type());
            if (!component.type().returnType().equals(descriptor.parameterType(i)))
                throw new IllegalArgumentException(String.format("Descriptor %s should match %d'th component %s", descriptor, i, component));
        }

        this.descriptor = descriptor;
        this.partial = partial;
        this.tryMatch = tryMatch;
        this.components = List.of(components);
    }

    @Override
    public MethodHandle tryMatch() {
        return tryMatch;
    }

    @Override
    public MethodHandle component(int i) {
        return components.get(i);
    }

    @Override
    public MethodType descriptor() {
        return descriptor;
    }

    @Override
    public boolean isPartial() {
        return partial;
    }
}
