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


package org.structr.web.entity.html;

import org.structr.common.PropertyView;
import org.structr.core.property.Property;
import org.structr.web.common.HtmlProperty;
import org.structr.web.entity.dom.DOMElement;

/**
 * @author Axel Morgner
 */
public class Embed extends DOMElement {

	public static final Property<String> _src		= new HtmlProperty("src");
	public static final Property<String> _type		= new HtmlProperty("type");
	public static final Property<String> _width		= new HtmlProperty("width");
	public static final Property<String> _height		= new HtmlProperty("height");

	public static final org.structr.common.View htmlView	= new org.structr.common.View(Embed.class, PropertyView.Html,
		_src, _type, _width, _height
	);
	
	//~--- get methods ----------------------------------------------------

	@Override
	public boolean isVoidElement() {

		return true;

	}
}
