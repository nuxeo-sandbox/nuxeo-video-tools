/*	ClosedCaptionsWidgetHandler.js */
/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere (https://github.com/ThibArg)
 */

/* Most of the code comes from:
 * 		http://jsbin.com/pdfjs-prevnext-v2/6865/edit#html,live
 * I added first/last, button enable/disable, etc.
 */

// Encapsulating everything to avoid collision with outside variable names
// (for example, ctx is used elsewhere in the browser)
function displayClosedCaptions(inDocId, inMainDivId, inTextAreaId) {

	var gMainDiv = jQuery("#" + inMainDivId);
	var gTextArea = jQuery("#" + inTextAreaId);
	
	var theURL = "/nuxeo/site/automation/ImageCropInDocument";
	jQuery.ajax({
		url		: theURL,
		type	: "POST",
		contentType: "application/json+nxrequest",
		data	: JSON.stringify(automationParams),
		headers	: {'X-NXVoidOperation': true}
	})
	.done( function() {
		console.log("done done done done");
	})
	.fail( function(jqXHR, textStatus, errorThrown) {
		//alert("Dommage. Essaye encore.");
		alert( "Request failed: " + textStatus )
	} );

} // displayPDF
//--EOF--


