Clazz.declarePackage ("JSV.popup");
Clazz.load (["J.popup.PopupResource"], "JSV.popup.JSVPopupResourceBundle", null, function () {
c$ = Clazz.declareType (JSV.popup, "JSVPopupResourceBundle", J.popup.PopupResource);
Clazz.overrideMethod (c$, "getMenuName", 
function () {
return "appMenu";
});
Clazz.makeConstructor (c$, 
function () {
Clazz.superConstructor (this, JSV.popup.JSVPopupResourceBundle, [null, null]);
});
Clazz.overrideMethod (c$, "buildStructure", 
function (menuStructure) {
this.addItems (JSV.popup.JSVPopupResourceBundle.menuContents);
this.addItems (JSV.popup.JSVPopupResourceBundle.structureContents);
if (menuStructure != null) this.setStructure (menuStructure, null);
}, "~S");
Clazz.overrideMethod (c$, "getWordContents", 
function () {
return [];
});
Clazz.overrideMethod (c$, "getMenuAsText", 
function (title) {
return this.getStuctureAsText (title, JSV.popup.JSVPopupResourceBundle.menuContents, JSV.popup.JSVPopupResourceBundle.structureContents);
}, "~S");
Clazz.defineStatics (c$,
"menuContents", [["appMenu", "_SIGNED_FileMenu Spectra... ShowMenu OptionsMenu ZoomMenu - Integration Peaks Measurements - Script... Properties"], ["appletMenu", "_SIGNED_FileMenu Spectra... - OptionsMenu ZoomMenu - Integration Peaks Measurements - Script... - Print... - AboutMenu"], ["_SIGNED_FileMenu", "Open_File... Open_Simulation... Open_URL... - Add_File... Add_Simulation... Add_URL... - Save_AsMenu Export_AsMenu - Close_Views Close_Simulations Close_All"], ["Save_AsMenu", "Original... JDXMenu CML XML(AnIML)"], ["JDXMenu", "XY DIF DIFDUP FIX PAC SQZ"], ["Export_AsMenu", "PDF - JAVAJPG PNG"], ["ShowMenu", "Show_Header Show_Source Show_Overlay_Key"], ["OptionsMenu", "Toggle_Grid Toggle_X_Axis Toggle_Y_Axis Toggle_Coordinates Toggle_Trans/Abs Reverse_Plot - Predicted_Solution_Colour Fill_Solution_Colour_(all)  Fill_Solution_Colour_(none)"], ["ZoomMenu", "Next_Zoom Previous_Zoom Reset_Zoom - Set_X_Scale... Reset_X_Scale"], ["AboutMenu", "VERSION"]],
"structureContents", [["Open_File...", "load ?"], ["Open_URL...", "load http://?"], ["Open_Simulation...", "load $?"], ["Add_File...", "load append ?"], ["Add_URL...", "load append http://?"], ["Add_Simulation...", "load append $?; view \"1HNMR\""], ["Close_All", "close all"], ["Close_Views", "close views"], ["Close Simulations", "close simulations"], ["Show_Header", "showProperties"], ["Show_Source", "showSource"], ["Show_Overlay_Key...", "showKey"], ["Next_Zoom", "zoom next;showMenu"], ["Previous_Zoom", "zoom prev;showMenu"], ["Reset_Zoom", "zoom clear"], ["Reset_X_Scale", "zoom out"], ["Set_X_Scale...", "zoom"], ["Spectra...", "view"], ["Overlay_Offset...", "stackOffsetY"], ["Script...", "script INLINE"], ["Properties", "showProperties"], ["Toggle_X_Axis", "XSCALEON toggle;showMenu"], ["Toggle_Y_Axis", "YSCALEON toggle;showMenu"], ["Toggle_Grid", "GRIDON toggle;showMenu"], ["Toggle_Coordinates", "COORDINATESON toggle;showMenu"], ["Reverse_Plot", "REVERSEPLOT toggle;showMenu"], ["Measurements", "SHOWMEASUREMENTS"], ["Peaks", "SHOWPEAKLIST"], ["Integration", "SHOWINTEGRATION"], ["Toggle_Trans/Abs", "IRMODE TOGGLE"], ["Predicted_Solution_Colour", "GETSOLUTIONCOLOR"], ["Fill_Solution_Colour_(all)", "GETSOLUTIONCOLOR fillall"], ["Fill_Solution_Colour_(none)", "GETSOLUTIONCOLOR fillallnone"], ["Print...", "print"], ["Original...", "write SOURCE"], ["CML", "write CML"], ["XML(AnIML)", "write XML"], ["XY", "write XY"], ["DIF", "write DIF"], ["DIFDUP", "write DIFDUP"], ["FIX", "write FIX"], ["PAC", "write PAC"], ["SQZ", "write SQZ"], ["JPG", "write JPG"], ["SVG", "write SVG"], ["PNG", "write PNG"], ["PDF", "write PDF"]]);
});
