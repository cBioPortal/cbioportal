/*
  This file is part of Cytoscape Web.
  Copyright (c) 2009, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
    - Agilent Technologies
    - Institut Pasteur
    - Institute for Systems Biology
    - Memorial Sloan-Kettering Cancer Center
    - National Center for Integrative Biomedical Informatics
    - Unilever
    - University of California San Diego
    - University of California San Francisco
    - University of Toronto

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 2.1 of the License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package org.cytoscapeweb.controller {
    import flare.vis.data.DataSprite;
    import flare.vis.data.NodeSprite;
	import flare.vis.data.EdgeSprite;
    
    import org.cytoscapeweb.ApplicationFacade;
    import org.cytoscapeweb.model.converters.ExternalObjectConverter;
    import org.cytoscapeweb.util.ExternalFunctions;
    import org.cytoscapeweb.util.Groups;
    import org.puremvc.as3.interfaces.INotification;
    

    /**
     * Handle the deselection of one or more nodes.
     * A nodes array must be sent as the body of the notification. 
     */
    public class DeselectCommand extends BaseSimpleCommand {
        
        override public function execute(notification:INotification):void {
            var arr:Array = notification.getBody() as Array;
            
            if (arr != null && arr.length > 0) {
                // Separate nodes and edges:
                var nodes:Array = [], edges:Array = [];
                for each (var ds:DataSprite in arr) {
					if(ds is NodeSprite){
						ds.props.detailFlag = false; // MODIFY to hide details of the nodes
						NodeSprite(ds).visitEdges(function(e:EdgeSprite):Boolean {
							graphMediator.resetDataSprite(e);
							return false;
						}); // MODIFY to recalculate edges for the undetailed nodes
					}					
                    if (ds is NodeSprite) nodes.push(ds);
                    else edges.push(ds);
                }
                
                // First remove the selection from the model:
                nodes = graphProxy.changeNodesSelection(nodes, false);
                edges = graphProxy.changeEdgesSelection(edges, false);

                // Then update the view:
                if (nodes.length > 0)
                    graphMediator.deselectNodes(nodes);
                if (edges.length > 0)
                    graphMediator.deselectEdges(edges);
                
                // Finally ,call the external lsiteners:
                var objs:Array, body:Object, type:String = "deselect";
                
                if (nodes.length > 0 && extMediator.hasListener(type, Groups.NODES)) {
                    objs = ExternalObjectConverter.toExtElementsArray(nodes);
                    body = { functionName: ExternalFunctions.INVOKE_LISTENERS, 
                             argument: { type: type, group: Groups.NODES, target: objs } };
                    
                    sendNotification(ApplicationFacade.CALL_EXTERNAL_INTERFACE, body);
                }
                
                if (edges.length > 0 && extMediator.hasListener(type, Groups.EDGES)) {
                    objs = ExternalObjectConverter.toExtElementsArray(edges);
                    body = { functionName: ExternalFunctions.INVOKE_LISTENERS, 
                             argument: { type: type, group: Groups.EDGES, target: objs } };

                    sendNotification(ApplicationFacade.CALL_EXTERNAL_INTERFACE, body);
                }

                if ((nodes.length > 0 || edges.length > 0) && extMediator.hasListener(type, Groups.NONE)) {
                    var all:Array = [];
                    all = all.concat(nodes).concat(edges);
                    
                    if (all.length > 0) {
                        objs = ExternalObjectConverter.toExtElementsArray(all);
                        body = { functionName: ExternalFunctions.INVOKE_LISTENERS, 
                                 argument: { type: type, group: Groups.NONE, target: objs } };
    
                        sendNotification(ApplicationFacade.CALL_EXTERNAL_INTERFACE, body);
                    }
                }
            }
        }
    }
}