/**
  * @desc loads xml document whose path is scpecified by 'filePath'
  * @param string filePath - path of the file that will be loaded
  * @return object - loaded xml object representation of the file content
*/
function loadXMLDoc(filePath) {
  if (window.XMLHttpRequest) {
    xhttp = new XMLHttpRequest();
  }
  else {
    xhttp = new ActiveXObject("Microsoft.XMLHTTP");
  }
  xhttp.open("GET", filePath, false);
  xhttp.send();
  return xhttp.responseXML;
};

/**
  * @desc converts given string into  xml representation.
  * @param string text - string representation of xml
  * @return object - xml object
*/
function textToXmlObject(text) {
  if (window.ActiveXObject) {
    var doc = new ActiveXObject('Microsoft.XMLDOM');
    doc.async = 'false';
    doc.loadXML(text);
  } else {
    var parser = new DOMParser();
    var doc = parser.parseFromString(text, 'text/xml');
  }
  return doc;
};


/**
  * @desc opens a modal window to display a message
  * @param string $msg - the message to be displayed
  * @return bool - success or failure
*/
function GraphMLToJSon(xml) {
  this.nodeKeys = {};
  this.edgeKeys = {};
  this.graphKeys = {};
  this.xmlDoc = xml;
  this.elements =
  {
    nodes: [],
    edges: []
  };
  this.fillKeys();
};


/**
  * @desc opens a modal window to display a message
  * @param string $msg - the message to be displayed
  * @return bool - success or failure
*/
GraphMLToJSon.prototype.fillKeys = function () {
  var x = this.xmlDoc.getElementsByTagName('key');
  for (i = 0; i < x.length; i++)
  {
    var type = x[i].getAttribute('attr.type');
    var id = x[i].getAttribute('id');
    var name = x[i].getAttribute('attr.name');
    var _for = x[i].getAttribute('for');

    var obj = {
      type: type,
      id: id,
      name: name,
      _for: _for
    };

    if (_for == "all") {
      this.nodeKeys[id] = obj;
      this.edgeKeys[id] = obj;
      this.graphKeys[id] = obj;
    }
    else if (_for == "node") {
      this.nodeKeys[id] = obj;
    }
    else if (_for == "edge") {
      this.edgeKeys[id] = obj;
    }
    else if (_for == "graph") {
      this.graphKeys[id] = obj;
    }
  }
};

/**
  * @desc opens a modal window to display a message
  * @param string $msg - the message to be displayed
  * @return bool - success or failure
*/
GraphMLToJSon.prototype.processEdge = function (theEdge)
{
    var id = $(theEdge).attr('id');

    var source = $(theEdge).attr('source');
    var target = $(theEdge).attr('target');
    var edgeData = $(theEdge).children('data');
    var cyData = {};

    //TODO WORKAROUND TO SOLVE Cytoscape.js problem where ids with spaces are not accepted
    source = source.replace(/[ ,'-]/g, "_");
    target = target.replace(/[ ,'-]/g, "_");

    cyData.id = id;
    cyData.source = source;
    cyData.target = target;

    for (var i = 0; i < edgeData.length; i++)
    {
      var data = edgeData[i];

      var keyId = $(data).attr("key");
      var val = $(data).text();
      var key = this.edgeKeys[keyId];
      if (key == null) {
        console.log("" + keyId + " is not a valid key for an edge");
        continue;
      }
      var type = key['type'];
      var name = key['name'];

      if (type == "int") {
        val = Number(val);
      }
      cyData[name] = val;

    }

  // Push edge here
  this.elements.edges.push({group:"edges",data: cyData});
}

/**
  * @desc opens a modal window to display a message
  * @param string $msg - the message to be displayed
  * @return bool - success or failure
*/
GraphMLToJSon.prototype.processNode = function (theNode, pid)
{
  var id = $(theNode).attr('id');

  //TODO WORKAROUND TO SOLVE Cytoscape.js problem where ids with spaces are not accepted
  id = id.replace(/[ ,'-]/g, "_");

  var nodeData = $(theNode).children('data');
  var cyData = {};
  cyData.id = id;

  if (pid != null) {
    cyData.parent = pid;
  }

  for (var i = 0; i < nodeData.length; i++)
  {
    var data = nodeData[i];

    var keyId = $(data).attr("key");
    var val = $(data).text();
    var key =  this.nodeKeys[keyId];
    var name = key['name'];

    if (key == null) {
      console.log("" + keyId + " is not a valid key for a node");
      continue;
    }

    var type = key['type'];
    var name = key['name'];

    if (type == "int") {
      val = Number(val);
    }
    cyData[name] = val;
  };

  //  Push node here
  this.elements.nodes.push({group:"nodes",data: cyData});

  var child = $(theNode).children('graph');
  if (child.length > 0) {
    child = child[0];

    var childNodes = $(child).children("node");

    for (var i = 0; i < childNodes.length; i++)
    {
      var theNode = $(childNodes)[i];
      this.processNode(theNode, id);
    }
  }
}

/**
  * @desc opens a modal window to display a message
  * @param string $msg - the message to be displayed
  * @return bool - success or failure
*/
GraphMLToJSon.prototype.toJSON = function () {
  var xmlObject = this.xmlDoc;
  var root = $(xmlObject).find("graph")[0];
  var childNodes = $(root).children("node");

  for (var i = 0; i < childNodes.length; i++) {
    var theNode = $(childNodes)[i];
    this.processNode(theNode, null);
  }

  var edges = $(root).children("edge");
  for (var i = 0; i < edges.length; i++) {
    var theEdge = $(edges)[i];
    this.processEdge(theEdge);
  }
  return this.elements;
};
