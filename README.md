nuxeo-video-tools
===========================

This plug-in adds tools for handling videos: Slice, Merge, Extract ClosedCaptions, ...

## About - Requirements
`nuxeo-video-tools` is a plug-in for the [nuxeo platform](http://www.nuxeo.com). It adds tools, operations, automation for handling videos: Slice, Merge, Extract ClosedCaptions...

Depending on the tool you are using, it may be rrequired to install some tools on your server: See the ["Third Party Tools Used" sections](#third-party-tools-used) 

## Table of Contents
* [Closed Captions](#closed-captions)
* [Operations](#operations)
  * [`Video: Extract Closed Captions`](#video-extract-closed-captions)
  * [`Video: Concat (ffmpeg demuxer)`](#video-concat-ffmpeg-demuxer)
  * [`Video: Convert`](#video-convert)
  * [`Video: Slice`](#video-slice)
  * [`Video: Slice in Parts`](#video-slice-in-parts)
  * [`Video: Watermark with picture`](#video-watermark-with-picture)
  * [Import Operations in your Studio Project](#import-operations-in-your-studio-project)
* [Layouts](#layouts)
* [Build-Install](#build-install)
* [Third Party Tools Used](#third-party-tools-used)
* [License](#license)
* [About Nuxeo](#about-nuxeo)

## Closed Captions
The plug-in requires `ccextractor` (see below, "Third Party Tools Used") to extract the closed captions from a video.

Once installed, the plug-in adds a _facet_ (`VideoClosedCaptions`) to the `Video` document (see `VideoToolsCoreTypes.xml`). This facets also holds a schema, `VideoClosedCaptions` (prefix `videocc`). This schema is used to store a blob of the closed captions after extraction. This blob is a plain text file.

Some details:

* A _listener_ is fired when the file of a Video document changes
* This listener starts an asynchronous worker which extracts the closed captions from the video:
  * If the video does not have closed captions, the fields of the `VideoClosedCaptions` schema stay null.
  * If the video does have closed captions, they are extracted and saved in the `VideoClosedCaptions` schema. As they are store as a plain text blob, they are indexed and can be found using a full text search
* When a video has closed captions extracted, the plug-in adds a "Closed Captions" tab, displaying the raw text of the closed captions

If you don't need close captionning and don't want to install `ccextractor` you must then add and XML extension which will deactivate the listener. So in your Studio project for example, you can add the following XML extension:

```
<extension target="org.nuxeo.ecm.core.event.EventServiceComponent" point="listener">
  <listener name="videoclosedcaptionslistener" enabled="false" />
</extension>
```

## Operations
### `Video: Extract Closed Captions`

#### input/output
Receives a blob or a document as input. It the input is a document, the operation gets the blob by reading the `xpath` parameter value (default value is the usual main file, `file:content`).

It returns a blob with the captions. Depending on the `outFormat` parameter, the blob will be a plain text blob or binary (see ccextractor documentation about this parameter). By default, the closed captions are extracted as "ttxt".

#### Parameters
* `outFormat`: The output format as expected by `ccextractor` (see its [documentation](http://ccextractor.sourceforge.net/using-ccextractor/command-line-usage.html)). For example, "srt", "txt", "ttxt", ... The default value is "ttxt".
* `startAt`: A string telling `ccextractor` where to start reading the closed captions`. Can be formatted as "MM:SS" or "HH:MM:SS"
* `endAt`: A string telling `ccextractor` where to ttop processing after the given time (same format as startAt)
* `xpath`: When the input is a Document, this parameter tells the operation where to find the blob (default value is `file:content`)
* `neverReturnNull`: When the video has no closed captions, it returns a null blob. This can break generic chains where the next operation is expecting a valid blob. If this parameter is `true`and the video has no closed captions, the operation returns a plain text empty blob whose name is "{original-file-name}-noCC.txt".


### `Video: Concat (ffmpeg demuxer)`

Receives a list of blobs (or a list of documents) holding a video and merge them, following the order of the list, in a single, final blob.

When the input is a list of documents, the operation reads the video file in the `file:content` field.

#### Important
* The operation uses the `ffmpeg demuxer`, so please, see to ffmpeg documentation about efficiency, frame rates, timestamps, compatibility, ...
* For example, mixing formats, sizes, frame rates ... will not create a nice final video.

#### Parameters
* `resultFileName`:
  * The name of the final video.
  * This parameter is optional. By default, the operation uses the name of the first video of the list and appends the "-concat" suffix before the file extension. So for example, if the name of the first video is "my video.mp4", the finale name will be "my video-concat.mp4"


### `Video: Convert`

Uses an existing nuxeo `converter` to convert (transcode) the video, optionaly changing its size.

A converter is declared in an XML extension and, most of the time, uses a command line to declare/use an external tool (typically, ffmpeg). Nuxeo already has some video converters: `convertToWebM`, `convertToMP4`, ... See [these contributions](http://explorer.nuxeo.com/nuxeo/site/distribution/current/viewContribution/org.nuxeo.ecm.platform.video.service.contrib--videoConversions).

`nuxeo-video-tools` adds an exemple of converter which uses the default classes of nuxeo, to show you can also contribute a converter without writing java code. You just need a `converter` which references a `commandLine`. See `VideoToolsConverters.xml` in this plugin, there is this `convertToAVI` which lets you convert your videos to AVI.

#### Parameters
* `height`: The new height of the transcoded video (width is adjusted proportionally)
* `scale`: Instead of passing a `height`, you can scale: `0.5` for example, means half the size.
* `converter`: The name of the converter to use.

Some details:

* If both `height` and `scale` are passed, only `height` is used
* If `height` (or `scale`) is <=0, the height/width are not modified

### `Video: Slice`
This operation receives a blob, cuts the video and outputs the result.

#### Parameters
* `start`: The start time. Using `ffmpeg` naming convention, so you can pass either raw seconds ("230" for example) or a formatted time "HH:MM:SS.sss".
* `duration`: The duration, formatted as `start`.
* `commandLine`: The `commandLine` contribution to use to cut the video. The default value is `videoSlicer` (it is defined in the `VideoToolsCommandLines.xml` file), which re-encode the video. The other command line  declared by the plug-in is `videoSlicerByCopy`, which does (basically) a raw copy of the part to extract. It is very, very fast but can lead to black frames at the end or the beginning, or troubles for ffmpeg to slice for the expected duration, etc.

### `Video: Slice in Parts`
Receives a blob as input. Cuts the video and produces in n parts of about the same `duration`. "about the same" because we are using default values and ffmeg does its best to cuit each part at the correct duration, but depending on timestamps, frame-rates, still images, ... ffmpeg will decide to cut a bit before or a bit after the expected duration.)

#### Parameters
* `duration`: the duration, in seconds, of each part.

### `video-watermark-with-picture`
Receives a blob (video) as input. Produces a blob watermarked with the given picture, at the given position.

#### Parameters
* `pictureDoc`: The id or the path of a document whose `file:content` field contains a the picture to use for the watermarking
* `x` and `y`:
  * The position from left and top of the video to start drawing the image (the top-left corner of the image will be positionned at y-x)
  * Default value for each: 0
* `resultFileName`:
  * The name of the resulting watermarked video.
  * **WARNING**: Do not change the video format vy passing another extension
  * Default value: original-file-name-WM.originalExtebnsion (so, for a video "myVideo.mp4", the final name will be "myVideo-WM.mp4")


### Import Operations in your Studio Project
To use these operations in your project, you must add their JSON definition to the `Automation Operations` registry.

To do that you can install the plug-in on you server, then go to `{your-server:port}/nuxeo/site/automation/doc`. For each operation you want to use, find it in the list, open it and open its JSON definition. Now, you can copy it and paste it the Studio registry. See [here](http://doc.nuxeo.com/x/hgM7)


## Layouts
Use /layouts/videobox.html as the fancybox main layout
Include two template widgets in the form displayed in the fancybox
Set the template type to /incl/videobox_end_timestamp.xhtml and /incl/videobox_start_timestamp.xhtml
Bind the two widgets to any string property

## Build-Install
Assuming [`maven`](http://maven.apache.org) (min. 3.2.1) is installed on your computer:

```
# Clone the GitHub repository
cd /path/to/where/you/want/to/clone/this/repository
git clone https://github.com/ThibArg/nuxeo-viedo-tools
# Compile
cd nuxeo-video-tools
mvn clean install
```

* The plug-in is in `nuxeo-video-tools/nuxeo-video-tools-plugin/target/`, its name is `nuxeo-video-tools-plugin-{version}.jar`.
* The Marketplace Package is in `nuxeo-video-tools/nuxeo-video-tools-mp/target`, its name is `nuxeo-video-tools-mp-{version}.zip`.

Also, if you just want the package without building it, just ask us :->

## Third Party Tools Used
* [`ffmpeg`](http://ffmpeg.org): ffmpeg is already requested by nuxeo for its default Video handling (storyboard, automatic transcoding, extraction of info), so it is likely you already have it installed. You can also check our [Installing and Setting Up Related Software](http://doc.nuxeo.com/x/zgJc) page.
* [`ccextractor`](http://ccextractor.sourceforge.net) is the tool used to extract the closed captions from a video (notice that Closed Captions and subtitles are not the same)


## License
(C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.

All rights reserved. This program and the accompanying materials
are made available under the terms of the GNU Lesser General Public License
(LGPL) version 2.1 which accompanies this distribution, and is available at
http://www.gnu.org/licenses/lgpl-2.1.html

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

Contributors:
Thibaud Arguillere (https://github.com/ThibArg)

## About Nuxeo

Nuxeo provides a modular, extensible Java-based [open source software platform for enterprise content management](http://www.nuxeo.com) and packaged applications for Document Management, Digital Asset Management and Case Management. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.
