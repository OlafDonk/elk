/*******************************************************************************
 * Copyright (c) 2010, 2015 Kiel University and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kiel University - initial API and implementation
 *******************************************************************************/
package org.eclipse.elk.alg.layered.p4nodes;

import org.eclipse.elk.alg.layered.ILayoutPhase;
import org.eclipse.elk.alg.layered.IntermediateProcessingConfiguration;
import org.eclipse.elk.alg.layered.graph.LGraph;
import org.eclipse.elk.alg.layered.graph.LNode;
import org.eclipse.elk.alg.layered.graph.Layer;
import org.eclipse.elk.alg.layered.intermediate.IntermediateProcessorStrategy;
import org.eclipse.elk.alg.layered.options.GraphProperties;
import org.eclipse.elk.alg.layered.options.InternalProperties;
import org.eclipse.elk.alg.layered.options.Spacings;
import org.eclipse.elk.core.math.KVector;
import org.eclipse.elk.core.util.IElkProgressMonitor;

/**
 * Node placement implementation that centers all nodes vertically.
 * 
 * <dl>
 *   <dt>Preconditions:</dt>
 *     <dd>The graph has a proper layering with optimized nodes ordering</dd>
 *     <dd>Ports are properly arranged</dd>
 *   <dt>Postconditions:</dt>
 *     <dd>Each node is assigned a vertical coordinate such that no two nodes overlap</dd>
 *     <dd>The size of each layer is set according to the area occupied by its nodes</dd>
 * </dl>
 * 
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public final class SimpleNodePlacer implements ILayoutPhase {

    /** additional processor dependencies for graphs with hierarchical ports. */
    private static final IntermediateProcessingConfiguration HIERARCHY_PROCESSING_ADDITIONS =
        IntermediateProcessingConfiguration.createEmpty()
            .addBeforePhase5(IntermediateProcessorStrategy.HIERARCHICAL_PORT_POSITION_PROCESSOR);

    /**
     * {@inheritDoc}
     */
    public IntermediateProcessingConfiguration getIntermediateProcessingConfiguration(
            final LGraph graph) {
        
        if (graph.getProperty(InternalProperties.GRAPH_PROPERTIES).contains(
                GraphProperties.EXTERNAL_PORTS)) {
            return HIERARCHY_PROCESSING_ADDITIONS;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void process(final LGraph layeredGraph, final IElkProgressMonitor monitor) {
        monitor.begin("Simple node placement", 1);

        Spacings spacings = layeredGraph.getProperty(InternalProperties.SPACINGS);
        
        // first iteration: determine the height of each layer
        double maxHeight = 0;
        for (Layer layer : layeredGraph.getLayers()) {
            KVector layerSize = layer.getSize();
            layerSize.y = 0;
            LNode lastNode = null;
            for (LNode node : layer.getNodes()) {
                if (lastNode != null) {
                    // use normal spacing as soon as a regular node is involved
                    layerSize.y += spacings.getVerticalSpacing(node, lastNode);
                }
                layerSize.y += node.getMargin().top + node.getSize().y + node.getMargin().bottom;
                lastNode = node;
            }
            maxHeight = Math.max(maxHeight, layerSize.y);
        }
        
        // second iteration: center the nodes of each layer around the tallest layer
        for (Layer layer : layeredGraph.getLayers()) {
            KVector layerSize = layer.getSize();
            double pos = (maxHeight - layerSize.y) / 2;
            LNode lastNode = null;
            for (LNode node : layer.getNodes()) {
                if (lastNode != null) {
                    pos += spacings.getVerticalSpacing(node, lastNode);
                }
                pos += node.getMargin().top;
                node.getPosition().y = pos;
                pos += node.getSize().y + node.getMargin().bottom;
                lastNode = node;
            }
        }
        
        monitor.done();
    }

}
