$ = jQuery
doneTypingInterval = 1000;
typingTimer = null;
autotransform = true;
transform = () ->
  jsRoutes.controllers.Application.transform().ajax(
    data: $("#files").serialize()
    success: (data) ->
      resulteditor.getSession().setValue data
      $("#result").val resulteditor.getSession().getValue()
    error: (err) ->
      console.log "error: " + err
  )

xmleditor = ace.edit "xmleditor"
xmleditor.setTheme "ace/theme/monokai"
xmleditor.getSession().setMode "ace/mode/xml"
xmleditor.getSession().setValue $("xml").val()
xmleditor.getSession().on(
  'change'
  ->
    $("#xml").val xmleditor.getSession().getValue()
    if autotransform
      clearTimeout typingTimer
      typingTimer = setTimeout(
        transform
        doneTypingInterval
      )
)

xsleditor = ace.edit "xsleditor"
xsleditor.setTheme "ace/theme/monokai"
xsleditor.getSession().setMode "ace/mode/xml"
xsleditor.getSession().setValue $("xsl").val()
xsleditor.getSession().on(
  'change'
  ->
    $("#xsl").val xsleditor.getSession().getValue()
    if autotransform
      clearTimeout typingTimer
      typingTimer = setTimeout(
        transform
        doneTypingInterval
      )
)

resulteditor = ace.edit "resulteditor"
resulteditor.setTheme "ace/theme/monokai"
resulteditor.getSession().setMode "ace/mode/xml"

$("#autotransform").click ->
  autotransform = !autotransform;
  $("#autotransform").find("i").toggleClass("fa-check-square-o");
  $("#autotransform").find("i").toggleClass("fa-square-o");


$("#new").click ->
  reset()

$("#transform").click ->
  if !autotransform
    transform()
$("#save").click ->
  jsRoutes.controllers.Application.save().ajax(
    data: $("#files").serialize()
    success: (data) ->
      if data[1] == "0"
        window.history.pushState("", "XSL Transform", "/" + data[0]);
      else
        window.history.pushState("", "XSL Transform", "/" + data[0] + "/" + data[1]);
      $("#id_slug").val(data[0]);
      $("#save").find("span").html("Update");
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
  $("#files").get(0).target = "_blank";
  $("#files").get(0).action = jsRoutes.controllers.Application.pdf().url;
  $("#files").get(0).submit();

$("#engines a").click ->
  `engine = $(this).data('engine');`
  updateEngine()
  transform()

updateEngine = ->
  $('#engine').val engine
  $('#engine-dropdown').text $('a[data-engine="'+engine+'"]').text()

reset = ->
  $("#save").find("span").html("Save");
  $("#id_slug").val("");
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
  window.history.pushState("", "XSL Transform", "/");

$ ->
  if id != ""
    $("#save").find("span").html("Update");

    jsRoutes.controllers.Application.xml(id, revision).ajax(
      dataType: 'text'
      success: (data) ->
        xmleditor.getSession().setValue data
        $("#xml").val(data)
    );
    jsRoutes.controllers.Application.xsl(id, revision).ajax(
      dataType: 'text'
      success: (data) ->
        xsleditor.getSession().setValue data
        $("#xsl").val(data)
    );
  else
    reset()

  clip = new ZeroClipboard($("#copyclipboard"), { moviePath: "/assets/flash/ZeroClipboard.swf" })
  clip.glue($("#copyclipboard"));

  updateEngine()

$("#copyclipboard").on(
  'mouseover'
  (event) ->
    $("#copyclipboard").attr("data-clipboard-text", "" + window.location);

)