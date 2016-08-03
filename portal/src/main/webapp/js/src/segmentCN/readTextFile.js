/**
* readTextFile
* @author: Linhong Chen
* @constructor
* date: Augst 2, 2016
* 
*/
ReadTextFile = function(_file){
    this.file = _file;
}

ReadTextFile.prototype.read = function(){
    var rawFile = new XMLHttpRequest();
    rawFile.open("GET", this.file, false);
    rawFile.onreadystatechange = function ()
    {
        if(rawFile.readyState === 4)
        {
            if(rawFile.status === 200 || rawFile.status == 0)
            {
                allText = rawFile.responseText;

            }
        }
    }
    rawFile.send(null);
}