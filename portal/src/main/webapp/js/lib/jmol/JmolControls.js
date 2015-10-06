// BH 10/6/2012 3:41:47 PM radios fixed
// BH 8:36 AM 7/27/2012  adds name/id for cmd button 
// BH 8/12/2012 6:51:53 AM adds function() {...} option for all controls:
//    Jmol.jmolButton(jmol, function(jmol) {...}, "xxxx")

(function(Jmol) {

	// private
	
	var c = Jmol.controls = {

		_hasResetForms: false,	
		_scripts: [""],
		_checkboxMasters: {},
		_checkboxItems: {},
	
		_buttonCount: 0,
		_checkboxCount: 0,
		_radioGroupCount: 0,
		_radioCount: 0,
		_linkCount: 0,
		_cmdCount: 0,
		_menuCount: 0,
		
		_previousOnloadHandler: null,	
		_control: null,
		_element: null,
		
		_appletCssClass: null,
		_appletCssText: "",
		_buttonCssClass: null,
		_buttonCssText: "",
		_checkboxCssClass: null,
		_checkboxCssText: "",
		_radioCssClass: null,
		_radioCssText: "",
		_linkCssClass: null,
		_linkCssText: "",
		_menuCssClass: null,
		_menuCssText: ""
	};

	c._addScript = function(appId,script) {
		if (!script)
			return 0;
		var index = c._scripts.length;
		c._scripts[index] = [appId, script];
		return index;
	}
	
	c._getIdForControl = function(appletOrId, script) {
		return (typeof appletOrId == "string" ? appletOrId 
		  : !script || appletOrId._canScript(script) ? appletOrId._id
			: null);
	}
		
	c._radio = function(appletOrId, script, labelHtml, isChecked, separatorHtml, groupName, id, title) {
		var appId = c._getIdForControl(appletOrId, script);
		if (appId == null)
			return null;
		++c._radioCount;
		groupName != undefined && groupName != null || (groupName = "jmolRadioGroup" + (c._radioGroupCount - 1));
		if (!script)
			return "";
		labelHtml != undefined && labelHtml != null || (labelHtml = script.substring(0, 32));
		separatorHtml || (separatorHtml = "");
		var scriptIndex = c._addScript(appId, script);
		var eospan = "</span>";
		var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><input name='"
		+ groupName + "' id='"+id+"' type='radio' onclick='Jmol.controls._click(this," +
					 scriptIndex + "," + appId + ");return true;' onmouseover='Jmol.controls._mouseOver(" +
					 scriptIndex + ");return true;' onmouseout='Jmol.controls._mouseOut()' " +
		 (isChecked ? "checked='true' " : "") + c._radioCssText + " />";
		if (labelHtml.toLowerCase().indexOf("<td>")>=0) {
			t += eospan;
			eospan = "";
		}
		t += "<label for=\"" + id + "\">" + labelHtml + "</label>" +eospan + separatorHtml;
		return t;
	}
	
/////////// events //////////

	c._scriptExecute = function(element, scriptInfo) {
		var applet = Jmol._applets[scriptInfo[0]];
		var script = scriptInfo[1];
		if (typeof(script) == "object")
			script[0](element, script, applet);
		else if (typeof(script) == "function")
		  script(applet);
		else
			Jmol.script(applet, script);
	}
	
	c._commandKeyPress = function(e, id, appId) {
		var keycode = (e == 13 ? 13 : window.event ? window.event.keyCode : e ? e.which : 0);
		if (keycode == 13) {
			var inputBox = document.getElementById(id)
			Jmol.controls._scriptExecute(inputBox, [appId, inputBox.value]);
		}
	}
	
	c._click = function(elementClicked, scriptIndex) {
		Jmol.controls._element = elementClicked;
		Jmol.controls._scriptExecute(elementClicked, Jmol.controls._scripts[scriptIndex]);
	}
	
	c._menuSelected = function(menuObject, appId) {
		var scriptIndex = menuObject.value;
		if (scriptIndex != undefined) {
			Jmol.controls._scriptExecute(menuObject, Jmol.controls._scripts[scriptIndex]);
			return;
		}
		var len = menuObject.length;
		if (typeof len == "number")
			for (var i = 0; i < len; ++i)
				if (menuObject[i].selected) {
					Jmol.controls._click(menuObject[i], menuObject[i].value, appId);
					return;
				}
		alert("?Que? menu selected bug #8734");
	}
		
	c._cbNotifyMaster = function(m){
		//called when a group item is checked
		var allOn = true;
		var allOff = true;
		for (var chkBox in m.chkGroup){
			if(m.chkGroup[chkBox].checked)
				allOff = false;
			else
				allOn = false;
		}
		if (allOn)m.chkMaster.checked = true;
		if (allOff)m.chkMaster.checked = false;
		if ((allOn || allOff) && Jmol.controls._checkboxItems[m.chkMaster.id])
			Jmol.controls._cbNotifyMaster(Jmol.controls._checkboxItems[m.chkMaster.id])
	}
	
	c._cbNotifyGroup = function(m, isOn){
		//called when a master item is checked
		for (var chkBox in m.chkGroup){
			var item = m.chkGroup[chkBox]
			item.checked = isOn;
			if (Jmol.controls._checkboxMasters[item.id])
				Jmol.controls._cbNotifyGroup(Jmol.controls._checkboxMasters[item.id], isOn)
		}
	}
	
	c._cbSetCheckboxGroup = function(chkMaster, chkbox){
		var id = chkMaster;
		if(typeof(id)=="number")id = "jmolCheckbox" + id;
		chkMaster = document.getElementById(id);
		if (!chkMaster)alert("jmolSetCheckboxGroup: master checkbox not found: " + id);
		var m = Jmol.controls._checkboxMasters[id] = {};
		m.chkMaster = chkMaster;
		m.chkGroup = {};
		for (var i = 1; i < arguments.length; i++){
			var id = arguments[i];
			if(typeof(id)=="number")id = "jmolCheckbox" + id;
			checkboxItem = document.getElementById(id);
			if (!checkboxItem)alert("jmolSetCheckboxGroup: group checkbox not found: " + id);
			m.chkGroup[id] = checkboxItem;
			Jmol.controls._checkboxItems[id] = m;
		}
	}
	
	c._cbClick = function(ckbox, whenChecked, whenUnchecked, applet) {
		var c = Jmol.controls;
		c._control = ckbox;
		c._click(ckbox, ckbox.checked ? whenChecked : whenUnchecked, applet);
		if(c._checkboxMasters[ckbox.id])
			c._notifyGroup(c._checkboxMasters[ckbox.id], ckbox.checked)
		if(c._checkboxItems[ckbox.id])
			c._notifyMaster(c._checkboxItems[ckbox.id])
	}
	
	c._cbOver = function(ckbox, whenChecked, whenUnchecked) {
		window.status = Jmol.controls._scripts[ckbox.checked ? whenUnchecked : whenChecked];
	}
	
	c._mouseOver = function(scriptIndex) {
		window.status = c._scripts[scriptIndex];
	}
	
	c._mouseOut = function() {
		window.status = " ";
		return true;
	}

// from JmolApplet

	c._onloadResetForms = function() {
		var c = Jmol.controls;
		// must be evaluated ONLY once -- is this compatible with jQuery?
		if (c._hasResetForms)
			return;
		c._hasResetForms = true;
		c._previousOnloadHandler = window.onload;
		window.onload = function() {
			var c = Jmol.controls;
			if (c._buttonCount+c._checkboxCount+c._menuCount+c._radioCount+c._radioGroupCount > 0) {
				var forms = document.forms;
				for (var i = forms.length; --i >= 0; )
					forms[i].reset();
			}
			if (c._previousOnloadHandler)
				c._previousOnloadHandler();
		}
	}

// from JmolApi

	c._getButton = function(appletOrId, script, label, id, title) {
		var c = Jmol.controls;
		var appId = c._getIdForControl(appletOrId, script);
		if (appId == null)
			return "";
		var c = Jmol.controls;
		//_jmolInitCheck();
		id != undefined && id != null || (id = "jmolButton" + c._buttonCount);
		label != undefined && label != null || (label = script.substring(0, 32));
		++c._buttonCount;
		var scriptIndex = c._addScript(appId, script);
		var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><input type='button' name='" + id + "' id='" + id +
						"' value='" + label +
						"' onclick='Jmol.controls._click(this," + scriptIndex +
						")' onmouseover='Jmol.controls._mouseOver(" + scriptIndex +
						");return true' onmouseout='Jmol.controls._mouseOut()' " +
						c._buttonCssText + " /></span>";
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}

	c._getCheckbox = function(appletOrId, scriptWhenChecked, scriptWhenUnchecked,
			labelHtml, isChecked, id, title) {

		var c = Jmol.controls;

		var appId = c._getIdForControl(appletOrId, scriptWhenChecked);
		if (appId != null)
			appId = c._getIdForControl(appletOrId, scriptWhenUnchecked);
		if (appId == null)
			return "";

		//_jmolInitCheck();
		id != undefined && id != null || (id = "jmolCheckbox" + c._checkboxCount);
		++c._checkboxCount;
		if (scriptWhenChecked == undefined || scriptWhenChecked == null ||
				scriptWhenUnchecked == undefined || scriptWhenUnchecked == null) {
			alert("jmolCheckbox requires two scripts");
			return;
		}
		if (labelHtml == undefined || labelHtml == null) {
			alert("jmolCheckbox requires a label");
			return;
		}
		var indexChecked = c._addScript(appId, scriptWhenChecked);
		var indexUnchecked = c._addScript(appId, scriptWhenUnchecked);
		var eospan = "</span>"
		var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><input type='checkbox' name='" + id + "' id='" + id +
						"' onclick='Jmol.controls._cbClick(this," +
						indexChecked + "," + indexUnchecked +
						")' onmouseover='Jmol.controls._cbOver(this," + indexChecked + "," +
						indexUnchecked +
						");return true' onmouseout='Jmol.controls._mouseOut()' " +
			(isChecked ? "checked='true' " : "")+ c._checkboxCssText + " />"
		if (labelHtml.toLowerCase().indexOf("<td>")>=0) {
			t += eospan
			eospan = "";
		}
		t += "<label for=\"" + id + "\">" + labelHtml + "</label>" +eospan;
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}

	c._getCommandInput = function(appletOrId, label, size, id, title) {
		var c = Jmol.controls;
		var appId = c._getIdForControl(appletOrId, "x");
		if (appId == null)
			return "";
		//_jmolInitCheck();
		id != undefined && id != null || (id = "jmolCmd" + c._cmdCount);
		label != undefined && label != null || (label = "Execute");
		size != undefined && !isNaN(size) || (size = 60);
		++c._cmdCount;
		var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><input name='" + id + "' id='" + id +
						"' size='"+size+"' onkeypress='Jmol.controls._commandKeyPress(event,\""+id+"\",\"" + appId + "\")'><input " +
						" type='button' name='" + id + "Btn' id='" + id + "Btn' value = '"+label+"' onclick='Jmol.controls._commandKeyPress(13,\""+id+"\",\"" + appId + "\")' /></span>";
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}

	c._getLink = function(appletOrId, script, label, id, title) {
		var c = Jmol.controls;
		var appId = c._getIdForControl(appletOrId, script);
		if (appId == null)
			return "";
		//_jmolInitCheck();
		id != undefined && id != null || (id = "jmolLink" + c._linkCount);
		label != undefined && label != null || (label = script.substring(0, 32));
		++c._linkCount;
		var scriptIndex = c._addScript(appId, script);
		var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><a name='" + id + "' id='" + id +
						"' href='javascript:Jmol.controls._click(this," + scriptIndex + ");' onmouseover='Jmol.controls._mouseOver(" + scriptIndex +
						");return true;' onmouseout='Jmol.controls._mouseOut()' " +
						c._linkCssText + ">" + label + "</a></span>";
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}

	c._getMenu = function(appletOrId, arrayOfMenuItems, size, id, title) {
		var c = Jmol.controls;
		var appId = c._getIdForControl(appletOrId, null);
		var optgroup = null;
		//_jmolInitCheck();
		id != undefined && id != null || (id = "jmolMenu" + c._menuCount);
		++c._menuCount;
		var type = typeof arrayOfMenuItems;
		if (type != null && type == "object" && arrayOfMenuItems.length) {
			var len = arrayOfMenuItems.length;
			if (typeof size != "number" || size == 1)
				size = null;
			else if (size < 0)
				size = len;
			var sizeText = size ? " size='" + size + "' " : "";
			var t = "<span id=\"span_"+id+"\""+(title ? " title=\"" + title + "\"":"")+"><select name='" + id + "' id='" + id +
							"' onChange='Jmol.controls._menuSelected(this,\"" + appId + "\")'" +
							sizeText + c._menuCssText + ">";
			for (var i = 0; i < len; ++i) {
				var menuItem = arrayOfMenuItems[i];
				type = typeof menuItem;
				var script = null;
				var text = null;
				var isSelected = null;
				if (type == "object" && menuItem != null) {
					script = menuItem[0];
					text = menuItem[1];
					isSelected = menuItem[2];
				} else {
					script = text = menuItem;
				}
				appId = c._getIdForControl(appletOrId, script);
				if (appId == null)
					return "";
				text == null && (text = script);
				if (script=="#optgroup") {
					t += "<optgroup label='" + text + "'>";
				} else if (script=="#optgroupEnd") {
					t += "</optgroup>";
				} else {
					var scriptIndex = c._addScript(appId, script);
					var selectedText = isSelected ? "' selected='true'>" : "'>";
					t += "<option value='" + scriptIndex + selectedText + text + "</option>";
				}
			}
			t += "</select></span>";
			if (Jmol._debugAlert)
				alert(t);
			return Jmol._documentWrite(t);
		}
	}
	
	c._getRadio = function(appletOrId, script, labelHtml, isChecked, separatorHtml, groupName, id, title) {
		//_jmolInitCheck();
		var c = Jmol.controls;
		if (c._radioGroupCount == 0)
			++c._radioGroupCount;
		var t = c._radio(appletOrId, script, labelHtml, isChecked, separatorHtml, groupName, (id ? id : groupName + "_" + Jmol._radioCount), title ? title : 0);
		if (t == null)
			return "";
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}
	
	c._getRadioGroup = function(appletOrId, arrayOfRadioButtons, separatorHtml, groupName, id, title) {
		/*
	
			array: [radio1,radio2,radio3...]
			where radioN = ["script","label",isSelected,"id","title"]
	
		*/
	
		//_jmolInitCheck();
		var type = typeof arrayOfRadioButtons;
		if (type != "object" || type == null || ! arrayOfRadioButtons.length) {
			alert("invalid arrayOfRadioButtons");
			return;
		}
		var c = Jmol.controls;
		separatorHtml != undefined && separatorHtml != null || (separatorHtml = "&nbsp; ");
		var len = arrayOfRadioButtons.length;
		++c._radioGroupCount;
		groupName || (groupName = "jmolRadioGroup" + (c._radioGroupCount - 1));
		var t = "<span id='"+(id ? id : groupName)+"'>";
		for (var i = 0; i < len; ++i) {
			if (i == len - 1)
				separatorHtml = "";
			var radio = arrayOfRadioButtons[i];
			type = typeof radio;
			var s = null;
			if (type == "object") {
				t += (s = c._radio(appletOrId, radio[0], radio[1], radio[2], separatorHtml, groupName, (radio.length > 3 ? radio[3]: (id ? id : groupName)+"_"+i), (radio.length > 4 ? radio[4] : 0), title));
			} else {
				t += (s = c._radio(appletOrId, radio, null, null, separatorHtml, groupName, (id ? id : groupName)+"_"+i, title));
			}
			if (s == null)
			  return "";
		}
		t+="</span>"
		if (Jmol._debugAlert)
			alert(t);
		return Jmol._documentWrite(t);
	}
	
	
})(Jmol);
