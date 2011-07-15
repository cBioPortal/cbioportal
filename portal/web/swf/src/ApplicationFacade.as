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
package org.cytoscapeweb {
    
    import org.cytoscapeweb.controller.*;
    import org.puremvc.as3.interfaces.IFacade;
    import org.puremvc.as3.patterns.facade.Facade;
    import org.puremvc.as3.patterns.observer.Notification;
    
    public class ApplicationFacade extends Facade implements IFacade {
        
        // ========[ CONSTANTS ]====================================================================
        
        // Notification name constants
        public static const STARTUP:String = "startup";
        public static const ADD_CALLBACKS:String = "add_callbacks";
        public static const CALL_EXTERNAL_INTERFACE:String = "call_external_interface";
        public static const INDETERMINATE_TASK_START:String = "indeterminate_task_start";
        public static const INDETERMINATE_TASK_COMPLETE:String = "indeterminate_task_complete";
        
        public static const ERROR:String = "error";
        public static const UPDATE_CURSOR:String = "update_cursor";
        public static const ENABLE_CUSTOM_CURSORS:String = "enable_custom_cursors";
        
        /** To load and display the graph. */
        public static const DRAW_GRAPH:String = "show_graph";
        
        /** 
         * To apply or reapply a layout to the graph.
         * The name of the Layout is the body of the notification. If it is not provided,
         * the current layout should be reapplied.
         */
        public static const APPLY_LAYOUT:String = "apply_layout";
        /** To apply another visual style to the network. The name of the Visual Style is the body of the notification */
        public static const SET_VISUAL_STYLE:String = "set_visual_style";
        /** To apply a visual style bypass to the network. A VsualStyleBypassVO must be sent as the notification body. */
        public static const SET_VISUAL_STYLE_BYPASS:String = "set_visual_style_bypass";
        
        /** To export the network as an image or an XML. */
        public static const EXPORT_NETWORK:String = "export_network";
        
        /** Ask to show or hide node/edge labels, according to the boolean value sent as the body of the notification. */
        public static const SHOW_LABELS:String = "show_labels";
        /** Ask to show or hide the pan-zoom control, according to the boolean value sent as the body of the notification. */
        public static const SHOW_PANZOOM_CONTROL:String = "show_panzoom_control";
        /** Ask to merge or unmerge edges, according to the boolean value sent as the body of the notification. */
        public static const MERGE_EDGES:String = "merge_edges";
        /** Ask to filter nodes or edges. */
        public static const FILTER:String = "filter_network";
        /** Ask to remove nodes/edges filter. */
        public static const REMOVE_FILTER:String = "remove_network_filter";
        
        /** To pan the whole network. */
        public static const ENABLE_GRAB_TO_PAN:String = "enable_grab_to_pan";
        public static const PAN_GRAPH:String = "pan_graph";
        public static const CENTER_GRAPH:String = "center_graph";
        
        /** Ask to zoom in or out the whole graph until it reaches the informed percent scale. */
        public static const ZOOM_GRAPH:String = "zoom_graph";
        public static const ZOOM_GRAPH_TO_FIT:String = "zoom_graph_to_fit";
        
        /** Ask to select one or more items. The body of the notification must contain the nodes or edges as an Array. */
        public static const SELECT:String = "select";
        /** Ask to select all edges and/or nodes (send the group [nodes|edges|none] as notification body). */
        public static const SELECT_ALL:String = "select_all";
        /** Ask to deselect one or more items. The body of the notification must contain the nodes or edges as an Array. */
        public static const DESELECT:String = "deselect";
        /** Ask to deselect all edges and/or nodes (send the group [nodes|edges|none] as notification body). */
        public static const DESELECT_ALL:String = "deselect_all";

        public static const REMOVE_ITEMS:String = "remove_items";
        public static const ADD_DATA_FIELD:String = "add_data_field";
        public static const REMOVE_DATA_FIELD:String = "remove_data_field";
        public static const UPDATE_DATA:String = "update_data";

        public static const CLICK_EVENT:String = "click_event";
        public static const DOUBLE_CLICK_EVENT:String = "double_click_event";
        public static const ROLLOVER_EVENT:String = "rollover_event";
        public static const ROLLOUT_EVENT:String = "rollout_event";
        public static const ACTIVATE_EVENT:String = "activate_event";
        public static const DEACTIVATE_EVENT:String = "deactivate_event";
        
        public static const GRAPH_DRAWN:String = "graph_drawn";
        public static const GRAPH_DATA_CHANGED:String = "graph_data_changed";
        public static const CONFIG_CHANGED:String = "config_changed";
        public static const VISUAL_STYLE_CHANGED:String = "visual_style_changed";
        public static const RESOURCE_BUNDLE_CHANGED:String = "resource_bundle_changed";
        public static const ZOOM_CHANGED:String = "zoom_changed";
        
        public static const EXT_INTERFACE_NOT_AVAILABLE:String = "ext_interface_not_available";
        public static const ADD_CALLBACK_ERROR:String = "add_callbacks_error";

        // ========[ PRIVATE PROPERTIES ]===========================================================
        
        // ========[ PUBLIC PROPERTIES ]============================================================
        
        // ========[ PUBLIC METHODS ]===============================================================
        
        /**
         * Broadcast the STARTUP Notification.
         */
        public function startup(app:CytoscapeWeb):void {
            notifyObservers(new Notification(STARTUP, app));
        }
        
        // ========[ PROTECTED METHODS ]============================================================
        
        /**
         * Register Commands with the Controller.
         */
        override protected function initializeController():void {
            // Call super to use the PureMVC Controller Singleton:
            super.initializeController();
            
            // Register all Commands:
            registerCommand(ERROR, ShowErrorCommand);
            registerCommand(STARTUP, StartupCommand);
            registerCommand(DRAW_GRAPH, DrawGraphCommand);
            registerCommand(SHOW_PANZOOM_CONTROL, ShowPanZoomControlCommand);
            registerCommand(ZOOM_GRAPH, ZoomGraphCommand);
            registerCommand(ZOOM_GRAPH_TO_FIT, ZoomGraphToFitCommand);
            registerCommand(SHOW_LABELS, ShowLabelsCommand);
            registerCommand(MERGE_EDGES, MergeEdgesCommand);
            registerCommand(ROLLOVER_EVENT, CBioHandleHoverCommand); // CBio Modification
            registerCommand(ROLLOUT_EVENT, CBioHandleHoverCommand); // CBio Modification
            registerCommand(CLICK_EVENT, HandleClickCommand);
            registerCommand(DOUBLE_CLICK_EVENT, HandleClickCommand);
            registerCommand(SELECT, SelectCommand);
            registerCommand(SELECT_ALL, SelectAllCommand);
            registerCommand(DESELECT, DeselectCommand);
            registerCommand(DESELECT_ALL, DeselectAllCommand);
            registerCommand(REMOVE_ITEMS, RemoveItemsCommand);
            registerCommand(ADD_DATA_FIELD, AddDataFieldCommand);
            registerCommand(REMOVE_DATA_FIELD, RemoveDataFieldCommand);
            registerCommand(UPDATE_DATA, UpdateDataCommand);
            registerCommand(FILTER, FilterCommand);
            registerCommand(REMOVE_FILTER, RemoveFilterCommand);
            registerCommand(GRAPH_DATA_CHANGED, HandleDataChangeCommand);
            registerCommand(SET_VISUAL_STYLE, SetVisualStyleCommand);
            registerCommand(SET_VISUAL_STYLE_BYPASS, SetVisualStyleBypassCommand);
            registerCommand(APPLY_LAYOUT, ApplyLayoutCommand);
            registerCommand(EXPORT_NETWORK, ExportNetworkCommand);
        }
        
        // ========[ SINGLETON STUFF ]==============================================================

        public function ApplicationFacade(lock:SingletonLock) {
        	super();
            if (lock == null)
                throw new Error( "Invalid Singleton access. Use ApplicationFacade.instance().");
        }
        
        public static function getInstance():ApplicationFacade {
            if (instance == null)
                instance = new ApplicationFacade(new SingletonLock());
            return instance as ApplicationFacade;
        }
    }
}

/**
 * This is a private class declared outside of the package that is only accessible 
 * to classes inside of the Model.as file.
 * Because of that, no outside code is able to get a reference to this class to pass
 * to the constructor, which enables us to prevent outside instantiation.
 */
class SingletonLock { }
