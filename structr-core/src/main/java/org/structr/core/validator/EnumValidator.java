/**
 * Copyright (C) 2010-2013 Axel Morgner, structr <structr@structr.org>
 *
 * This file is part of structr <http://structr.org>.
 *
 * structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.core.validator;

import java.util.LinkedHashSet;
import java.util.Set;
import org.structr.common.SecurityContext;
import org.structr.core.property.PropertyKey;
import org.structr.common.error.EmptyPropertyToken;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.ValueToken;
import org.structr.core.GraphObject;
import org.structr.core.PropertyValidator;

//~--- classes ----------------------------------------------------------------

/**
 * A validator that ensures a given String value is a valid enum element of the
 * given type.
 *
 * @author Christian Morgner
 */
public class EnumValidator<T> implements PropertyValidator<T> {

	private Set<T> values = new LinkedHashSet<T>();

	public EnumValidator(T[] values) {
		for(T t : values) {
			this.values.add(t);
		}
	}

	@Override
	public boolean isValid(SecurityContext securityContext, GraphObject object, PropertyKey<T> key, T value, ErrorBuffer errorBuffer) {

		if (value == null) {

			errorBuffer.add(object.getType(), new EmptyPropertyToken(key));

		} else {

			if(values.contains(value)) {

				return true;

			} else {
				
				errorBuffer.add(object.getType(), new ValueToken(key, values.toArray()));
			}
		}

		return false;
	}
	
	@Override
	public boolean requiresSynchronization() {
		return false;
	}
}
