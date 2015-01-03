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

// Encapsulating everything to avoid collision with outside variable names
// (for example, ctx is used elsewhere in the browser)
function displayClosedCaptions(inDocId, inMainDivId, inTextAreaId) {

	var gMainDiv = jQuery("#" + inMainDivId);
	var gTextArea = jQuery("#" + inTextAreaId);
	
	var theURL = "/nuxeo/nxbigfile/default/" + inDocId + "/videocc:content/";
	jQuery.ajax({
		url		: theURL,
		type	: "GET",
		/*contentType: "application/json",*/
		headers	: {'Accept': "text/plain"}
	})
	.done( function(data, textStatus, jqXHR) {
		console.log("done done done done");
		gTextArea.val(data);
	})
	.fail( function(jqXHR, textStatus, errorThrown) {
		//alert("Dommage. Essaye encore.");
		alert( "Request failed: " + textStatus );
	} );

} // displayClosedCaptions



