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


package org.structr.core.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.tooling.GlobalGraphOperations;

import org.structr.common.SecurityContext;
import org.structr.common.error.FrameworkException;
import org.structr.core.Result;
import org.structr.core.Services;
import org.structr.core.entity.AbstractNode;
import org.structr.core.graph.search.Search;
import org.structr.core.graph.search.SearchAttribute;
import org.structr.core.graph.search.SearchNodeCommand;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Node;
import org.structr.core.EntityContext;
import org.structr.core.converter.PropertyConverter;
import org.structr.core.module.ModuleService;
import org.structr.core.property.PropertyKey;

//~--- classes ----------------------------------------------------------------

/**
 * Change the property key from the old to the new value on all nodes matching the type.
 * 
 * Example: "email":"foo@bar.com" => "eMail":"foo@bar.com"
 * 
 * If no type property is found, change the property key on all nodes.
 * If a property with the new key is already present, the command will abort.
 *
 * @author Axel Morgner
 */
public class BulkChangeNodePropertyKeyCommand extends NodeServiceCommand implements MaintenanceCommand {

	private static final Logger logger = Logger.getLogger(BulkChangeNodePropertyKeyCommand.class.getName());

	//~--- methods --------------------------------------------------------

	@Override
	public void execute(final Map<String, Object> properties) throws FrameworkException {

		final GraphDatabaseService graphDb     = (GraphDatabaseService) arguments.get("graphDb");
		final SecurityContext superUserContext = SecurityContext.getSuperUserInstance();
		final NodeFactory nodeFactory          = new NodeFactory(superUserContext);
		final SearchNodeCommand searchNode     = Services.command(superUserContext, SearchNodeCommand.class);
		
		
		String type		= null;
		final String oldKey	= (String) properties.get("oldKey");
		final String newKey	= (String) properties.get("newKey");
		
		if (graphDb != null && StringUtils.isNotBlank(oldKey) && StringUtils.isNotBlank(newKey)) {

			Result<AbstractNode> nodes = null;

			if (properties.containsKey(AbstractNode.type.dbName())) {

				type = (String) properties.get(AbstractNode.type.dbName());
				List<SearchAttribute> attrs = new LinkedList<SearchAttribute>();

				attrs.add(Search.andExactType(EntityContext.getEntityClassForRawType(type)));

				nodes = searchNode.execute(attrs);

				properties.remove(AbstractNode.type.dbName());

			} else {

				nodes = nodeFactory.instantiateAll(GlobalGraphOperations.at(graphDb).getAllNodes());
			}

			final Class cls = Services.getService(ModuleService.class).getNodeEntityClass(type);
			
			long nodeCount = bulkGraphOperation(securityContext, nodes.getResults(), 1000, "ChangeNodePropertyKey", new BulkGraphOperation<AbstractNode>() {

				@Override
				public void handleGraphObject(SecurityContext securityContext, AbstractNode node) {

					// Treat only "our" nodes
					if (node.getProperty(AbstractNode.uuid) != null) {

						for (Entry entry : properties.entrySet()) {

							String key = (String) entry.getKey();
							Object val = null;
							
							// allow to set new type
							if (key.equals("newType")) {
								key = "type";
							}

							PropertyConverter inputConverter = EntityContext.getPropertyKeyForJSONName(cls, key).inputConverter(securityContext);

							
							if (inputConverter != null) {
								try {
									val = inputConverter.convert(entry.getValue());
								} catch (FrameworkException ex) {
									logger.log(Level.SEVERE, null, ex);
								}
								
							} else {
								val = entry.getValue();
							}

							PropertyKey propertyKey = EntityContext.getPropertyKeyForDatabaseName(node.getClass(), key);
							if (propertyKey != null) {
									
								Node dbNode = node.getNode();

								if (dbNode.hasProperty(newKey)) {

									logger.log(Level.SEVERE, "Node {0} has already a property with key {1}", new Object[] { node, newKey });
									throw new IllegalStateException("Node has already a property of the new key");

								}

								if (dbNode.hasProperty(oldKey)) {

									dbNode.setProperty(newKey, dbNode.getProperty(oldKey));
									dbNode.removeProperty(oldKey);

								}
								
								node.updateInIndex();
									
							}

						}

					}
				}

				@Override
				public void handleThrowable(SecurityContext securityContext, Throwable t, AbstractNode node) {
					logger.log(Level.WARNING, "Unable to set properties of node {0}: {1}", new Object[] { node.getUuid(), t.getMessage() } );
				}

				@Override
				public void handleTransactionFailure(SecurityContext securityContext, Throwable t) {
					logger.log(Level.WARNING, "Unable to set node properties: {0}", t.getMessage() );
				}
			});


			logger.log(Level.INFO, "Fixed {0} nodes ...", nodeCount);
		
		} else {
			
			logger.log(Level.INFO, "No values for oldKey and/or newKey found, aborting.");
			
		}

		logger.log(Level.INFO, "Done");
	}

}
