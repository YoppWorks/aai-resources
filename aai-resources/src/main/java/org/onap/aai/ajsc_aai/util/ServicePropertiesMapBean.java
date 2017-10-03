/**
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */
package org.onap.aai.ajsc_aai.util;

import org.onap.aai.ajsc_aai.filemonitor.ServicePropertiesMap;

public class ServicePropertiesMapBean {

	/**
	 * Gets the property.
	 *
	 * @param propFileName the prop file name
	 * @param propertyKey the property key
	 * @return the property
	 */
	public static String getProperty(String propFileName, String propertyKey) {
		return ServicePropertiesMap.getProperty(propFileName, propertyKey);
	}
}