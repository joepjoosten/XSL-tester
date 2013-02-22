var typingTimer;
var doneTypingInterval = 1000;
var transform = function() {
    jsRoutes.controllers.Application.transform().ajax({
        data: $("#files").serialize(),
        success: function (data) {
            resulteditor.getSession().setValue(data);
        },
        error: function (err) {
            console.log(err);
        }
    });
}
var xmleditor = ace.edit("xmleditor");
var xml = $("#xml");
xmleditor.setTheme("ace/theme/monokai");
xmleditor.getSession().setMode("ace/mode/xml");
xmleditor.getSession().setValue(xml.val());
xmleditor.getSession().on('change', function () {
    xml.val(xmleditor.getSession().getValue());
    clearTimeout(typingTimer);
    typingTimer = setTimeout(transform, doneTypingInterval);
});

var xsleditor = ace.edit("xsleditor");
var xsl = $("#xsl");
xsleditor.setTheme("ace/theme/monokai");
xsleditor.getSession().setMode("ace/mode/xml");
xsleditor.getSession().on('change', function () {
    xsl.val(xsleditor.getSession().getValue());
    clearTimeout(typingTimer);
    typingTimer = setTimeout(transform, doneTypingInterval);
});

var resulteditor = ace.edit("resulteditor");
resulteditor.setTheme("ace/theme/monokai");
resulteditor.getSession().setMode("ace/mode/xml");

$("#transform").click(function () {
    transform();
});

$("#pdf").click(function () {
    var form = $("#files");
    form.action = "/pdf";
    form.target = "_blank";
    form.submit();
});

$(function(){
    jsRoutes.controllers.Application.defaultXML().ajax({
        dataType: 'text',
        success: function(data){
            xmleditor.getSession().setValue(data);
            xml.val(xmleditor.getSession().getValue());
        }
    });
    jsRoutes.controllers.Application.defaultXSL().ajax({
        dataType: 'text',
        success: function(data){
            xsleditor.getSession().setValue(data);
            xsl.val(xsleditor.getSession().getValue());
        }
    });
})