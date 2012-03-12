/*
 *  Copyright (C) 2012 Axel Morgner
 * 
 *  This file is part of structr <http://structr.org>.
 * 
 *  structr is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  structr is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with structr.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.structr.web.entity.html;

import org.neo4j.graphdb.Direction;
import org.structr.common.PropertyView;
import org.structr.common.RelType;
import org.structr.core.EntityContext;
import org.structr.core.entity.DirectedRelation;
import org.structr.web.entity.Content;

/**
 * @author Axel Morgner
 */
public class Form extends HtmlElement {

	private static final String[] htmlAttributes = new String[] { "accept-charset", "action", "autocomplete", "enctype", "method", "name", "novalidate", "target" };
	
	static {
		EntityContext.registerPropertySet(Form.class, PropertyView.All,		HtmlElement.UiKey.values());
		EntityContext.registerPropertySet(Form.class, PropertyView.Public,	HtmlElement.UiKey.values());
		EntityContext.registerPropertySet(Form.class, PropertyView.Html, true,	htmlAttributes);

		EntityContext.registerEntityRelation(Form.class, Div.class,	RelType.CONTAINS, Direction.INCOMING, DirectedRelation.Cardinality.ManyToMany);

		EntityContext.registerEntityRelation(Form.class, Content.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		EntityContext.registerEntityRelation(Form.class, Div.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		EntityContext.registerEntityRelation(Form.class, Input.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		EntityContext.registerEntityRelation(Form.class, Button.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		EntityContext.registerEntityRelation(Form.class, Select.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		EntityContext.registerEntityRelation(Form.class, Label.class,	RelType.CONTAINS, Direction.OUTGOING, DirectedRelation.Cardinality.ManyToMany);
		
	}
}