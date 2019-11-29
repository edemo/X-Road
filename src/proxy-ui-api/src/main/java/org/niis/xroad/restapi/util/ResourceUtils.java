/**
 * The MIT License
 * Copyright (c) 2018 Estonian Information System Authority (RIA),
 * Nordic Institute for Interoperability Solutions (NIIS), Population Register Centre (VRK)
 * Copyright (c) 2015-2017 Estonian Information System Authority (RIA), Population Register Centre (VRK)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.restapi.util;

import org.apache.commons.io.IOUtils;
import org.niis.xroad.restapi.exceptions.ErrorDeviation;
import org.niis.xroad.restapi.openapi.BadRequestException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Resource utils
 */
public final class ResourceUtils {
    public static final String ERROR_INVALID_CERT_UPLOAD = "invalid_cert_upload";

    private ResourceUtils() {
        // noop
    }

    /**
     * Read bytes from {@link Resource}. Also handles closing the stream.
     * @param resource
     * @return byte array
     * @throws BadRequestException
     */
    public static byte[] springResourceToBytesOrThrowBadRequest(Resource resource) {
        byte[] certificateBytes;
        try (InputStream is = resource.getInputStream()) {
            certificateBytes = IOUtils.toByteArray(is);
        } catch (IOException ex) {
            throw new BadRequestException("cannot read certificate data", ex,
                    new ErrorDeviation(ERROR_INVALID_CERT_UPLOAD));
        }
        return certificateBytes;
    }
}