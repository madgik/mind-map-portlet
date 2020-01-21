<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" isELIgnored="true" %>
<%@ page import="com.liferay.portal.util.PortalUtil"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%>

<portlet:defineObjects />

<html manifest="cache.appcache">
<head>
<meta charset="UTF-8" />
<!-- <meta name="viewport" -->
<!-- 	content="width=device-width, initial-scale=1, maximum-scale=1"> -->

<script src="https://code.jquery.com/jquery-3.3.1.slim.min.js"
	integrity="sha256-3edrmyuQ0w65f8gfBsqowzjJe2iM6n0nKciPUp8y+7E="
	crossorigin="anonymous"></script>
<script crossorigin
	src="https://cdn.jsdelivr.net/npm/jsmind@0.4.6/js/jsmind.min.js"></script>
<script crossorigin
	src="https://cdn.jsdelivr.net/npm/jsmind@0.4.6/js/jsmind.screenshot.min.js"></script>

<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/overrides.css?1.0.5" />" />

<!-- <meta name="description" -->
<!-- 	content="mindmaps is an HTML5 based mind mapping app. It lets you create neat looking mind maps in the browser." /> -->
<!-- <meta name="keywords" -->
<!-- 	content="mind maps html5 mindmaps offline easy intuitive" /> -->
<!-- <meta name="google" content="notranslate" /> -->
<!-- <title>mindmaps</title> -->
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/common.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/app.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/Aristo/jquery-ui-1.8.7.custom.css" />" />
<link rel="stylesheet" type="text/css"
	href="<c:url value="/css/minicolors/jquery.miniColors.css" />" />

<script id="template-float-panel" type="text/x-jquery-tmpl">
<div class="ui-widget ui-dialog ui-corner-all ui-widget-content float-panel no-select">
  <div class="ui-dialog-titlebar ui-widget-header ui-helper-clearfix">
    <span class="ui-dialog-title">${title}</span>
    <a class="ui-dialog-titlebar-close ui-corner-all" href="#" role="button">
      <span class="ui-icon"></span>
    </a>
  </div>
  <div class="ui-dialog-content ui-widget-content">
  </div>
</div>
</script>

<script id="template-notification" type="text/x-jquery-tmpl">
<div class="notification">
  {{if closeButton}}
  <a href="#" class="close-button">x</a>
  {{/if}}
  {{if title}}
  <h1 class="title">{{html title}}</h1>
  {{/if}}
  <div class="content">{{html content}}</div>
</div>
</script>

<script id="template-open-table-item" type="text/x-jquery-tmpl">
<tr>
  <td><a class="title" href="#">${title}</a></td>
  <td>${$item.format(dates.modified)}</td>
  <td><a class="delete" href="#">delete</a></td>
</tr>
</script>

<script id="template-open-table-item-ws" type="text/x-jquery-tmpl">
<tr>
  <td><a class="title" href="#">${name}</a></td>
</tr>
</script>

<script id="template-open" type="text/x-jquery-tmpl">
<div id="open-dialog" class="file-dialog" title="Open mind map">
	<h1>From workspace</h1>
  <p>Choose a mind map from the workspace.</p>
 <table class="workspacestorage-filelist">
    <thead>
      <tr>
        <th class="title">Name</th>
      </tr>
    </thead>
    <tbody class="ws-document-list"></tbody>
  </table>
<div class="seperator"></div>
  <h1>Local Storage</h1>
  <p>This is a list of all mind maps that are saved in your browser's local storage. Click on the title of a map to open it.</p>
  <table class="localstorage-filelist">
    <thead>
      <tr>
        <th class="title">Title</th>
        <th class="modified">Last Modified</th>
        <th class="delete"></th>
      </tr>
    </thead>
    <tbody class="document-list"></tbody>
  </table>
  <div class="seperator"></div>
  <h1>From file</h1>
  <p>Choose a mind map from your computer's hard drive.</p>
  <div class="file-chooser">
    <input type="file" />
  </div>
</div>
</script>

<script id="template-save" type="text/x-jquery-tmpl">
<div id="save-dialog" class="file-dialog" title="Save mind map">
<h1>To workspace</h1>
  <p>Save the mind map to your workspace.</p>
  <button id="button-save-workspace">Save</button>
<div class="seperator"></div>
  <h1>Local Storage</h1>
  <p>You can save your mind map in your browser's local storage. Be aware that this is still experimental: the space is limited and there is no guarantee that the browser will keep this document forever. Useful for frequent backups in combination with cloud storage.</p>
  <button id="button-save-localstorage">Save</button>
  <input type="checkbox" class="autosave" id="checkbox-autosave-localstorage">
  <label for="checkbox-autosave-localstorage">Save automatically every minute.</label>
  <div class="seperator"></div>
  <h1>To file</h1>
  <p>Save the mind map as a file on your computer.</p>
  <button id="button-save-hdd">Save</button>
</div>
</script>

<script id="template-navigator" type="text/x-jquery-tmpl">
<div id="navigator">
  <div class="active">
    <div id="navi-content">
      <div id="navi-canvas-wrapper">
        <canvas id="navi-canvas"></canvas>
        <div id="navi-canvas-overlay"></div>
      </div>
      <div id="navi-controls">
        <span id="navi-zoom-level"></span>
        <div class="button-zoom" id="button-navi-zoom-out"></div>
        <div id="navi-slider"></div>
        <div class="button-zoom" id="button-navi-zoom-in"></div>
      </div>
    </div>
  </div>
  <div class="inactive">
  </div>
</div>
</script>


<script id="template-inspector" type="text/x-jquery-tmpl">
<div id="inspector">
  <div id="inspector-content">
    <table id="inspector-table">
      <tr>
        <td>Font size:</td>
        <td><div
            class="buttonset buttons-very-small buttons-less-padding">
            <button id="inspector-button-font-size-decrease">A-</button>
            <button id="inspector-button-font-size-increase">A+</button>
          </div></td>
      </tr>
      <tr>
        <td>Font style:</td>
        <td><div
            class="font-styles buttonset buttons-very-small buttons-less-padding">
            <input type="checkbox" id="inspector-checkbox-font-bold" /> 
            <label
            for="inspector-checkbox-font-bold" id="inspector-label-font-bold">B</label>
              
            <input type="checkbox" id="inspector-checkbox-font-italic" /> 
            <label
            for="inspector-checkbox-font-italic" id="inspector-label-font-italic">I</label> 
            
            <input
            type="checkbox" id="inspector-checkbox-font-underline" /> 
            <label
            for="inspector-checkbox-font-underline" id="inspector-label-font-underline">U</label> 
            
            <input
            type="checkbox" id="inspector-checkbox-font-linethrough" />
             <label
            for="inspector-checkbox-font-linethrough" id="inspector-label-font-linethrough">S</label>
          </div>
        </td>
      </tr>
      <tr>
        <td>Font color:</td>
        <td><input type="hidden" id="inspector-font-color-picker"
          class="colorpicker" /></td>
      </tr>
      <tr>
        <td>Branch color:</td>
        <td><input type="hidden" id="inspector-branch-color-picker"
          class="colorpicker" />
          <button id="inspector-button-branch-color-children" title="Apply branch color to all children" class="right buttons-small buttons-less-padding">Inherit</button>
        </td>
      </tr>
    </table>
  </div>
</div>
</script>

<script id="template-export-map" type="text/x-jquery-tmpl">
<div id="export-map-dialog" title="Export mind map">
  <h2 class='image-description'>To download the map right-click the
    image and select "Save Image As"</h2>
  <div id="export-preview"></div>
</div>
</script>
</head>
<body>

<p id="portletInfo" data-namespace="<portlet:namespace/>" data-login-url="<portlet:resourceURL />" hidden></p>
<portlet:resourceURL var="baseResourceUrl" ></portlet:resourceURL>
	<div id="mindmapsportlet-container">
		<script type="text/javascript">
        	window.staticFileBaseUrl = '<%=request.getContextPath()%>/static/';
		</script>
		<div id="print-area">
			<p class="print-placeholder">Please use the print option from the
				mind map menu</p>
		</div>

		<div id="container">
			
			<div id="canvas-container">
				<div id="drawing-area" class="no-select"></div>
			</div>
			<div id="topbar">
				<div id="toolbar">

					<div class="buttons">
						<span class="buttons-left"> </span> <span class="buttons-right">
						</span>
					</div>

				</div>
			</div>
			<div id="bottombar">

				<div id="statusbar">
					<div
						class="buttons buttons-right buttons-small buttons-less-padding"></div>
				</div>
			</div>
		</div>

	</div>
	
	<script type="text/javascript"
		src="//ajax.googleapis.com/ajax/libs/jquery/1.6.1/jquery.min.js"></script>
	<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/linkify.min.js"></script>
	<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/linkify-jquery.min.js"></script>
	<script type="text/javascript"
		src="//api.filestackapi.com/filestack.js"></script>
	<script type="text/javascript"
		src="//cdnjs.cloudflare.com/ajax/libs/FileSaver.js/1.3.3/FileSaver.min.js"></script>

	<script type="text/javascript"
		src="<%=request.getContextPath()%>/js/script.js"></script>
</body>
</html>
