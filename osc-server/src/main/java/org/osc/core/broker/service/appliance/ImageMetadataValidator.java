/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.core.broker.service.appliance;

import org.apache.log4j.Logger;
import org.osc.core.broker.model.image.ImageMetadata;
import org.osc.core.broker.service.exceptions.VmidcBrokerValidationException;
import org.osc.core.broker.util.VersionUtil;
import org.osc.core.broker.util.VersionUtil.Version;
import org.osc.core.common.virtualization.VirtualizationType;

public class ImageMetadataValidator {

    private static final Logger log = Logger.getLogger(ImageMetadataValidator.class);

    public void validate(ImageMetadata imageMetadata, boolean isPolicyMappingSupported) throws Exception {
        ImageMetadata.checkForNullFields(imageMetadata);

        Version minIscVersion = imageMetadata.getMinIscVersion();
        int compareValue = VersionUtil.getVersion().compareTo(minIscVersion);
        if (compareValue != 0) {
            if (compareValue < 1) {
                throw new VmidcBrokerValidationException("This Appliance is compatible with OSC server version "
                        + minIscVersion.getShortVersionStrWithBuild() + " or higher");
            } else if (compareValue > 1) {
                // TODO: Future. We fulfill minimum software version. Enforce other validations here.
            }
        }

        try {
            imageMetadata.getManagerType();
            VirtualizationType virtualizationType = imageMetadata.getVirtualizationType();
            imageMetadata.getEncapsulationTypes();
            if (virtualizationType == null) {
                throw new IllegalArgumentException();
            } else if (virtualizationType.isOpenstack()) {
                imageMetadata.getOpenstackVirtualizationVersion();
            }
        } catch (IllegalArgumentException iae) {
            log.error("Invalid manager type/virtualization type/virtualization version/encapsulation type", iae);
            throw new VmidcBrokerValidationException(
                    "Invalid File Format. Invalid Manager Type and/or Virtualization Type and/or Virtualization Version and/or Encapsulation Type.");
        }

        if (isPolicyMappingSupported && imageMetadata.getVirtualizationType().isOpenstack()
                && imageMetadata.getEncapsulationTypes().isEmpty()
                ) {
            throw new VmidcBrokerValidationException(
                    "Invalid File Format. Encapsulation Types cannot be empty for Openstack Virtualization Type.");
        }

    }
}
