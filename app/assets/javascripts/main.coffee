$ = jQuery
doneTypingInterval = 1000;
typingTimer = null;
transform = () ->
  console.log $("#files").serialize()
  jsRoutes.controllers.Application.transform().ajax(
    data: $("#files").serialize()
    success: (data) ->
      resulteditor.getSession().setValue data
      $("#result").val resulteditor.getSession().getValue()
    error: (err) ->
      console.log "error: " + err
  )

xmleditor = ace.edit "xmleditor"
xmleditor.setTheme  "ace/theme/monokai"
xmleditor.getSession().setMode  "ace/mode/xml"
xmleditor.getSession().setValue $("xml").val()
xmleditor.getSession().on(
  'change'
  ->
    $("#xml").val xmleditor.getSession().getValue()
    clearTimeout typingTimer
    typingTimer = setTimeout(
      transform
      doneTypingInterval
    )
  )

xsleditor = ace.edit "xsleditor"
xsleditor.setTheme  "ace/theme/monokai"
xsleditor.getSession().setMode  "ace/mode/xml"
xsleditor.getSession().setValue $("xsl").val()
xsleditor.getSession().on(
  'change'
  ->
    $("#xsl").val xsleditor.getSession().getValue()
    clearTimeout typingTimer
    typingTimer = setTimeout(
      transform
      doneTypingInterval
    )
)

resulteditor = ace.edit "resulteditor"
resulteditor.setTheme  "ace/theme/monokai"
resulteditor.getSession().setMode  "ace/mode/xml"

$("#transform").click ->
  transform()
$("#save").click ->
  jsRoutes.controllers.Application.save().ajax(
    data: $("#files").serialize()
    success: (data) ->
     console.log(data);
    error: (err) ->
      console.log err
      $("#alert").find("h4").html("Save oeps");
      $("#alert").find("p").html(err.responseText);
      $("#alert").alert();
      $("#alert").slideDown();
      setTimeout(
        ->
          $('#alert').slideUp()
        4000
      )
  )

$("#pdf").click ->
  jsRoutes.controllers.Application.pdf().ajax(
    data: $("#files").serialize()
    success: (data) ->
      window.open(
        'data:application/pdf;base64,' + data
        '_blank'
      );
    error: (err) ->
      console.log err
      $("#alert").find("h4").html("PDF Generation Error!");
      $("#alert").find("p").html(err.responseText);
      $("#alert").alert();
      $("#alert").slideDown();
      setTimeout(
        ->
          $('#alert').slideUp()
        4000
      )
  )

$ ->
  if url != ""
    jsRoutes.controllers.Application.xml(url).ajax(
      dataType: 'text'
      success: (data) ->
        xmleditor.getSession().setValue data
        $("#xml").val(data)
    );
    jsRoutes.controllers.Application.xsl(url).ajax(
      dataType: 'text'
      success: (data) ->
        xsleditor.getSession().setValue data
        $("#xsl").val(data)
    );
  else
    jsRoutes.controllers.Application.defaultXML().ajax(
      dataType: 'text'
      success: (data) ->
        xmleditor.getSession().setValue data
        $("#xml").val(data)
    );
    jsRoutes.controllers.Application.defaultXSL().ajax(
      dataType: 'text'
      success: (data) ->
        xsleditor.getSession().setValue data
        $("#xsl").val(data)
    );